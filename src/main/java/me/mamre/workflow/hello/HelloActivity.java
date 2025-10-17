package me.mamre.workflow.hello;


import io.temporal.activity.ActivityInterface;
import me.mamre.model.Person;

@ActivityInterface
public interface HelloActivity {
    String hello(Person person);
}