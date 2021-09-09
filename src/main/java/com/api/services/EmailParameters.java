package com.api.services;

import lombok.Data;

@Data
public class EmailParameters {
  public String  recipientAddress;
    public String  from;
    public String  subject;
    public String body;
    public String enviado;
}
