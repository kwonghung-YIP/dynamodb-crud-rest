package org.hung.poc.aws;

import java.util.UUID;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CustomerNotFoundException extends RuntimeException {

    private final UUID id;
}
