package cn.arrebyte.ai.mcpserver.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wch
 * @description
 * @date 2026/6/15 16:31
 */
@Data
@Builder
public class WeatherInfo implements Serializable {
    private String city;        // 城市
    private String temperature; // 温度
    private String condition;   // 天气状况
    private String humidity;    // 湿度
    private String wind;        // 风力
    private String advice;      // 出行建议
}
