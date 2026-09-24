package com.rickysurya.commonplace.service;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
public class SubmissionService {


    // two submission cases for now
    // url
    // text
    public boolean textSubmission(String text) {
        //store the text directly to postgres
        return true;
    }

    public boolean urlSubmission(String url) {
        //call fetcher to retrieve the content of the url
        return true;
    }
    public void check(String submission) {
        UrlValidator urlValidator = new UrlValidator();
        if (!urlValidator.isValid(submission)) {
            textSubmission(submission);
        } else {
            urlSubmission(submission);
        }
    }
}
