package cn.arrebyte.ai.mcpserver.entity;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author wch
 * @description
 * @date 2026/6/15 16:09
 */
@Data
@Builder
public class CityBaseInfo implements Serializable {
    private String name;
    private String country;
    private Integer population;
    private Double area;
    private String description;
}
