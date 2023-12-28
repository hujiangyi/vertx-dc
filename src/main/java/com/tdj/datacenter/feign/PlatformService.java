package com.tdj.datacenter.feign;

import com.tdj.datacenter.annotation.FeignService;
import com.tdj.datacenter.domain.EntUser;
import com.tdj.datacenter.domain.Oauth2Token;
import com.tdj.datacenter.domain.Result;
import feign.Headers;
import feign.RequestLine;
import io.vertx.core.Future;


@FeignService("ty-platform")
@Headers({ "Accept: application/json" })
public interface PlatformService {
    @RequestLine("POST /platform/Login/doEntLogin")
    @Headers("Content-Type: application/json")
    Future<Result<Oauth2Token>> doEntLogin(EntUser entUser);
}

