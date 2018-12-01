package br.edu.ulbra.election.voter.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class ElectionClientService {

    private final ElectionClient electionClient;

    @Autowired
    public ElectionClientService(ElectionClient electionClient) {
        this.electionClient = electionClient;
    }

    public Long countVotesByVoterId(Long voterId){
        return electionClient.countVotesByVoterId(voterId);
    }

    @FeignClient(value = "election-service", url = "http://localhost:8084")
    private interface ElectionClient{
        @GetMapping("/v1/vote/voter/{voterId}")
        Long countVotesByVoterId(@PathVariable(name = "voterId") Long voterId);

    }

}
