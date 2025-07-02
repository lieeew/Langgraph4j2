package com.leikooo.langchain4.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UrlResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 7816656942628946440L;

    private Boolean success;

    private String data;

}