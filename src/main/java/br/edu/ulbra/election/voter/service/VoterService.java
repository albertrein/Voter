package br.edu.ulbra.election.voter.service;

import br.edu.ulbra.election.voter.client.ElectionClientService;
import br.edu.ulbra.election.voter.exception.GenericOutputException;
import br.edu.ulbra.election.voter.input.v1.VoterInput;
import br.edu.ulbra.election.voter.model.Voter;
import br.edu.ulbra.election.voter.output.v1.GenericOutput;
import br.edu.ulbra.election.voter.output.v1.VoterOutput;
import br.edu.ulbra.election.voter.repository.VoterRepository;
import br.edu.ulbra.election.voter.validations.ValidateName;
import feign.FeignException;
import org.apache.commons.lang.StringUtils;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.util.List;

@Service
public class VoterService {

    private final VoterRepository voterRepository;
    private final ElectionClientService electionClientService;
    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    private static final String MESSAGE_INVALID_ID = "Invalid id";
    private static final String MESSAGE_VOTER_NOT_FOUND = "Voter not found";

    @Autowired
    public VoterService(VoterRepository voterRepository, ElectionClientService electionClientService, ModelMapper modelMapper, PasswordEncoder passwordEncoder){
        this.voterRepository = voterRepository;
        this.electionClientService = electionClientService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<VoterOutput> getAll(){
        Type voterOutputListType = new TypeToken<List<VoterOutput>>(){}.getType();
        return modelMapper.map(voterRepository.findAll(), voterOutputListType);
    }

    public VoterOutput create(VoterInput voterInput) {
        validateInput(voterInput, false);
        checkEmailDuplicate(voterInput.getEmail(), null);
        Voter voter = modelMapper.map(voterInput, Voter.class);
        voter.setPassword(passwordEncoder.encode(voter.getPassword()));
        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public VoterOutput getById(Long voterId){
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        return modelMapper.map(voter, VoterOutput.class);
    }

    public VoterOutput update(Long voterId, VoterInput voterInput) {
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }
        validateInput(voterInput, true);
        checkEmailDuplicate(voterInput.getEmail(), voterId);

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }

        voter.setEmail(voterInput.getEmail());
        voter.setName(voterInput.getName());
        if (!StringUtils.isBlank(voterInput.getPassword())) {
            voter.setPassword(passwordEncoder.encode(voterInput.getPassword()));
        }
        voter = voterRepository.save(voter);
        return modelMapper.map(voter, VoterOutput.class);
    }

    public GenericOutput delete(Long voterId) {
        if (voterId == null){
            throw new GenericOutputException(MESSAGE_INVALID_ID);
        }

        Voter voter = voterRepository.findById(voterId).orElse(null);
        if (voter == null){
            throw new GenericOutputException(MESSAGE_VOTER_NOT_FOUND);
        }
        countVotesByVoterId(voterId);
        voterRepository.delete(voter);

        return new GenericOutput("Voter deleted");
    }

    private void checkEmailDuplicate(String email, Long currentVoter){
        Voter voter = voterRepository.findFirstByEmail(email);
        if (voter != null && !voter.getId().equals(currentVoter)){
            throw new GenericOutputException("Duplicate email");
        }
    }

    private void countVotesByVoterId(Long voterId){
        //validando se o eleitor já votou
        try{
            if(electionClientService.countVotesByVoterId(voterId) > 0){
                throw new GenericOutputException("Voter already voted");
            }
        }catch (FeignException e){
            if(e.status() == 0){
                throw new GenericOutputException("Election not found");
            }
            if(e.status() == 500){
                throw new GenericOutputException("Valor Invalido");
            }
        }
    }

    private void validateInput(VoterInput voterInput, boolean isUpdate){
        if (StringUtils.isBlank(voterInput.getEmail())){
            throw new GenericOutputException("Invalid email");
        }
        if(voterRepository.findFirstByEmail(voterInput.getEmail())!=null){
            if(!isUpdate) {
                throw new GenericOutputException(" Existent email");
            }
        }

        if (StringUtils.isBlank(voterInput.getName())){
            throw new GenericOutputException("Invalid name");
        }

        if(!ValidateName.validateName(voterInput.getName())){
            throw new GenericOutputException("Invalid name, name must contain a last name");
        }
        if (StringUtils.isBlank(voterInput.getName()) || voterInput.getName().trim().length() < 5 ) {
            throw new GenericOutputException("Invalid name, name must contain at least 5 characters");
        }

        if (!StringUtils.isBlank(voterInput.getPassword())){
            if (!voterInput.getPassword().equals(voterInput.getPasswordConfirm())){
                throw new GenericOutputException("Passwords doesn't match");
            }

            voterInput.setPassword(passwordEncoder.encode(voterInput.getPassword()));
        } else {
            if (!isUpdate) {
                throw new GenericOutputException("Password doesn't match");
            }
        }
    }

}
