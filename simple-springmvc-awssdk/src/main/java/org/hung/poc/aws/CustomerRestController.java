package org.hung.poc.aws;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.service.annotation.PutExchange;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer")
public class CustomerRestController {

    private final CustomerRepository repo;

    @GetMapping("/{id}")
    public Customer findById(@PathVariable UUID id) {
        return repo.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @GetMapping("/")
    public List<Customer> findAll() {
        return repo.findAll();
    }

    @PostMapping("/")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Customer create(@RequestBody Customer customer) {
        return repo.create(customer);
    }

    @PutExchange("/{id}")
    public Customer update(@PathVariable UUID id, @RequestBody Customer customer) {
        repo.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        return repo.updated(customer);
    }

    @DeleteMapping("/{id}")
    public Customer deleteById(@PathVariable UUID id) {
        repo.findById(id).orElseThrow(() -> new CustomerNotFoundException(id));
        return repo.deleteById(id);
    }

    @ExceptionHandler
    public ProblemDetail customerNotFound(CustomerNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problem.setType(ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri());
        problem.setDetail("Customer not found:["+e.getId()+"]");
        return problem;
    }

}
