package me.mamre.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class FlowAction {
    private String action;
    private String compensateBy;
    private int retries;
    private int startToCloseSec;
    private int next;
}