package com.ly.common.net.converter;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.ly.common.frame.BaseApp;
import com.ly.common.net.AsyncRequests;
import com.ly.common.net.RespCode;
import com.ly.common.net.respEntity.BaseResponse;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

/**
 * @author ly
 * date 2019/8/10 11:04
 */
public class GsonRespBodyConverter<T> implements Converter<ResponseBody, T> {
    private final Gson gson;
    private final TypeAdapter<T> adapter;

    GsonRespBodyConverter(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
    }

    @Override
    public T convert(@NotNull ResponseBody value) throws IOException {
        JsonReader jsonReader = gson.newJsonReader(value.charStream());
        try {
            T result = adapter.read(jsonReader);
            if (result instanceof BaseResponse) {
                BaseResponse baseResponse = (BaseResponse) result;
                switch (baseResponse.code) {
                    case RespCode.UNAUTHORIZED:
                    case RespCode.TOKEN_ERROR:
                    case RespCode.TOKEN_EXPIRED:

                        break;
                    case RespCode.DU_NON_EXISTENT://设备未激活

                        break;
                    default:

                        break;
                }
            }
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw new JsonIOException("JSON document was not fully consumed.");
            }
            return result;
        } finally {
            value.close();
        }
    }
}
