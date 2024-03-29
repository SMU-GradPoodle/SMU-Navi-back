package smu.poodle.smnavi.tipoff.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import smu.poodle.smnavi.common.errorcode.CommonErrorCode;
import smu.poodle.smnavi.common.errorcode.DetailErrorCode;
import smu.poodle.smnavi.common.errorcode.ErrorCode;
import smu.poodle.smnavi.common.exception.RestApiException;
import smu.poodle.smnavi.tipoff.domain.Thumb;
import smu.poodle.smnavi.tipoff.domain.ThumbStatus;
import smu.poodle.smnavi.tipoff.domain.TipOff;
import smu.poodle.smnavi.tipoff.dto.LikeInfoDto;
import smu.poodle.smnavi.tipoff.repository.ThumbsRepository;
import smu.poodle.smnavi.user.domain.UserEntity;
import smu.poodle.smnavi.user.sevice.LoginService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ThumbService {
    private final ThumbsRepository thumbsRepository;
    private final LoginService loginService;

    @Transactional
    public LikeInfoDto doLikeOrHate(Long tipOffId, ThumbStatus thumbStatus) {
        Long userId = loginService.getLoginMemberId();

        if(userId == 0) {
            throw new RestApiException(CommonErrorCode.LOGIN_REQUIRED);
        }

        thumbsRepository.findByUserIdAndTipOffId(userId, tipOffId)
                .ifPresentOrElse((thumb -> {
                            if (thumb.getThumbStatus().equals(thumbStatus)) {
                                thumbsRepository.delete(thumb);
                            } else {
                                thumb.setThumbStatus(thumbStatus);
                            }
                        }),

                        () -> thumbsRepository.save(Thumb.builder()
                                .user(UserEntity.builder().id(userId).build())
                                .tipOff(TipOff.builder().id(tipOffId).build())
                                .thumbStatus(thumbStatus)
                                .build()));

        return getLikeInfo(tipOffId);
    }

    @Transactional
    public LikeInfoDto getLikeInfo(Long tipOffId) {
        Long userId = loginService.getLoginMemberId();

        Long likeCount = thumbsRepository.getLikeCount(tipOffId);
        Long hateCount = thumbsRepository.getHateCount(tipOffId);

        Optional<Thumb> optionalThumb = thumbsRepository.findByUserIdAndTipOffId(userId, tipOffId);

        return new LikeInfoDto(likeCount, hateCount, optionalThumb);
    }
}
