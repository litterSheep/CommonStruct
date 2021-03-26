package com.ly.common.net;


import com.ly.common.net.respEntity.BaseResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * @author ly
 * date 2019/8/7 10:52
 */
public interface IRequests {

    @POST("sign/dus/register")
    Call<BaseResponse> registerDevice(@Body Object params);

    @GET("home/scenicInfo/{scenicId}")
    Call<BaseResponse<Object>> getScenicDetailInfo(@Path("scenicId") String scenicId);

    @POST("sign/dus/register")
    Call<BaseResponse> uploadOpenLog();
}
