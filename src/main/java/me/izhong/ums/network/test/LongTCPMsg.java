package me.izhong.ums.network.test;

import lombok.Data;


@Data
public class LongTCPMsg {
    private String payload;

    public LongTCPMsg(String payload) {
        this.payload = payload;
    }

}
