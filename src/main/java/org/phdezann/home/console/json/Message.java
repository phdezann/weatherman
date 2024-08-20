package org.phdezann.home.console.json;

import java.time.LocalDateTime;

import org.phdezann.home.console.bus.MsgEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Message {
    private MsgEnum event;
    private String payload;
    private LocalDateTime lastTouch;
    private LocalDateTime creation;
}
