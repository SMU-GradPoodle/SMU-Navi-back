package smu.poodle.smnavi.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import smu.poodle.smnavi.common.errorcode.StatusCode;
import smu.poodle.smnavi.common.exception.RestApiException;

import static smu.poodle.smnavi.common.errorcode.CommonStatusCode.CREATED;
import static smu.poodle.smnavi.common.errorcode.CommonStatusCode.OK;

@Getter
@Builder(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BaseResponse<T> {
    @JsonIgnore
    StatusCode statusCode;

    String code;
    String message;
    T data;

    public static BaseResponse<Void> ok() {
        return BaseResponse.<Void>builder()
                .statusCode(OK)
                .build();
    }

    public static <T> BaseResponse<T> ok(T data) {
        return BaseResponse.<T>builder()
                .statusCode(OK)
                .data(data)
                .build();
    }


    public static <T> BaseResponse<T> created(T data) {
        return BaseResponse.<T>builder()
                .statusCode(CREATED)
                .data(data)
                .build();
    }

    public static <T> BaseResponse<T> fail(RestApiException restApiException) {
        return BaseResponse.<T>builder()
                .statusCode(restApiException.getStatusCode())
                .build();
    }


    private static <T> CustomBaseResponseBuilder<T> builder() {
        return new CustomBaseResponseBuilder<>();
    }

    private static class CustomBaseResponseBuilder<T> extends BaseResponseBuilder<T> {
        @Override
        public BaseResponse<T> build() {
            BaseResponse<T> baseResponse = super.build();

            StatusCode status = baseResponse.statusCode;
            baseResponse.code = status.code();
            baseResponse.message = status.message();
            return baseResponse;
        }
    }
}
