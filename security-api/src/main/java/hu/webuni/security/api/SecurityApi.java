package hu.webuni.security.api;

import hu.webuni.security.api.dto.LoginDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="security", url = "${fein.security.url}")
public interface SecurityApi {

    @PostMapping("/api/login")
    String login(@RequestBody LoginDto loginDto);
}
