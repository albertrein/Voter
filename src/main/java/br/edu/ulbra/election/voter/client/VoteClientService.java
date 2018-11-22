package br.edu.ulbra.election.voter.client;

import br.edu.ulbra.election.voter.output.v1.GenericOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Service
public class VoteClientService {
    private final VoteClient voteClient;

    @Autowired
    public VoteClientService(VoteClient voteClient){
        this.voteClient = voteClient;
    }

    public GenericOutput getById(Long id) {
        return this.voteClient.getById(id);
    }

    @FeignClient(value="candidate-service", url="http://localhost:8084")
    private interface VoteClient {

        @GetMapping("/v1/vote/{voteId}")
        GenericOutput getById(@PathVariable(name = "voteId") Long Id);

    }
}
