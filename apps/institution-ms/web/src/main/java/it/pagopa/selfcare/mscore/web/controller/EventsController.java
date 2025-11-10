package it.pagopa.selfcare.mscore.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import it.pagopa.selfcare.mscore.core.EventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping(value = "/events", produces = MediaType.APPLICATION_JSON_VALUE)
@Api(tags = "Events")
@Slf4j
@RequiredArgsConstructor
public class EventsController {

    private final EventsService eventsService;

    @ApiOperation(value = "${swagger.mscore.events.delegations}", notes = "${swagger.mscore.events.delegations}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PostMapping(value = "/delegations")
    public ResponseEntity<Void> sendDelegationEvents(@ApiParam("${swagger.mscore.events.fromDate}")
                                                     @RequestParam(name = "fromDate") OffsetDateTime fromDate) {
        eventsService.sendDelegationEvents(fromDate);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
