package me.mamre.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Flow {
    private String id;
    private String name;
    private String description;
    private List<FlowAction> actions;
    private String workflow;
}