package smu.poodle.smnavi.map.callapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import smu.poodle.smnavi.map.enums.MonitoringBus;
import smu.poodle.smnavi.common.util.XmlApiUtil;
import smu.poodle.smnavi.map.redis.repository.BusPositionRedisRepository;
import smu.poodle.smnavi.map.redis.hash.BusPosition;
import smu.poodle.smnavi.map.service.BusPositionService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class BusPositionApi {

    private final BusPositionRedisRepository busPositionRedisRepository;
    private final BusPositionService busPositionService;

    @Value("${PUBLIC_DATA_API_KEY}")
    private String SERVICE_KEY;

    private String getUrl(MonitoringBus monitoringBus) {
        final String URL = "http://ws.bus.go.kr/api/rest/buspos/getBusPosByRouteSt?ServiceKey=%s&busRouteId=%s&startOrd=%d&endOrd=%d";

        return String.format(
                URL,
                SERVICE_KEY,
                monitoringBus.getBusRouteId(),
                monitoringBus.getMonitoringStartStationOrder(),
                monitoringBus.getMonitoringEndStationOrder());
    }

    @Transactional
    @Scheduled(cron = "0/30 * 6-20 * * *")
    public void cachingBusPosition() {
        log.info(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS")) + " 버스 교통 이슈 확인 시작 시간");

        Document xmlContent = XmlApiUtil.getRootTag(getUrl(MonitoringBus.BUS_7016));
        Element msgBody = (Element) xmlContent.getElementsByTagName("msgBody").item(0);

        NodeList itemList = msgBody.getElementsByTagName("itemList");

        List<BusPosition> busPositionList = new ArrayList<>();

        for (int i = 0; i < itemList.getLength(); i++) {
            Element element = (Element) itemList.item(i);

            String licensePlate = element.getElementsByTagName("plainNo").item(0).getTextContent();
            String gpsX = element.getElementsByTagName("tmX").item(0).getTextContent();
            String gpsY = element.getElementsByTagName("tmY").item(0).getTextContent();
            int sectionOrder = Integer.parseInt(element.getElementsByTagName("sectOrd").item(0).getTextContent());

            busPositionList.add(BusPosition.builder()
                    .licensePlate(licensePlate)
                    .sectionOrder(sectionOrder)
                    .gpsX(gpsX)
                    .gpsY(gpsY)
                    .hasIssue(false)
                    .build());
        }

        ZonedDateTime now = ZonedDateTime.now();
        if (now.getMinute() % 6 == 0 && now.getSecond() < 10) {
            log.info(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd HH:mm:ss.SSS")) + " 버스 교통 이슈 확인");
            busPositionService.catchAccidentInfo(busPositionList);
        } else {
            Iterable<BusPosition> busPositionIterable = busPositionRedisRepository.findAll();

            Map<String, BusPosition> busPositionMap = new HashMap<>();

            for (BusPosition busPosition : busPositionIterable) {
                busPositionMap.put(busPosition.getLicensePlate(), busPosition);
            }

            for (BusPosition busPosition : busPositionList) {
                BusPosition cachedBusPosition = busPositionMap.getOrDefault(busPosition.getLicensePlate(), null);

                if (cachedBusPosition != null && cachedBusPosition.getHasIssue()) {
                    busPosition.setHasIssue(true);
                }
            }
        }
        busPositionRedisRepository.deleteAll();
        busPositionRedisRepository.saveAll(busPositionList);
    }

    @Scheduled(cron = "* 5 21 * * *")
    public void deleteBusPositionCache() {
        busPositionRedisRepository.deleteAll();
    }
}
