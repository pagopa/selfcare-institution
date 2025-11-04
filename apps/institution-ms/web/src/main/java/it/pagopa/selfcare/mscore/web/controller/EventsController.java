package it.pagopa.selfcare.mscore.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.mscore.core.EventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@RestController
@RequestMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Events")
@Slf4j
@RequiredArgsConstructor
public class EventsController {

    private final EventsService eventsService;

    @PostMapping(value = "/delegations")
    public void sendDelegationEvents(@ApiParam("${swagger.mscore.events.fromDate}")
                                     @RequestParam(name = "fromDate") OffsetDateTime fromDate) {
        eventsService.sendDelegationEvents(fromDate);
    }

}
