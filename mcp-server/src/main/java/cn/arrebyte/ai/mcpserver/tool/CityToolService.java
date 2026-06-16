package cn.arrebyte.ai.mcpserver.tool;

import cn.arrebyte.ai.mcpserver.entity.CityBaseInfo;
import cn.arrebyte.ai.mcpserver.entity.WeatherInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wch
 * @description
 * @date 2026/6/15 16:08
 */
@Service
public class CityToolService implements InitializingBean {
    public static final Logger LOGGER = LoggerFactory.getLogger(CityToolService.class);

    private static final Map<String, CityBaseInfo> CITY_INFO_MAP = new HashMap();


    @Tool(description = "根据城市名称查询城市的基本信息", name = "getCityInfo")
    public CityBaseInfo getCityInfo(@ToolParam(description = "城市名称，例如：北京、上海") String cityName){
        LOGGER.info("MCP-SERVER --> getCityInfo");
        CityBaseInfo cityBaseInfo = CITY_INFO_MAP.get(cityName);
        if (cityBaseInfo == null){
            return CityBaseInfo.builder()
                    .name(cityName)
                    .country("未知")
                    .population(0)
                    .area(0.0)
                    .description("抱歉，没有找到该城市的信息")
                    .build();
        }
        return cityBaseInfo;
    }

    /**
     * 工具2: 查询城市天气
     * 演示多个工具的使用
     */
    @Tool(name = "getCityWeather",
            description = "查询指定城市的当前天气情况")
    public WeatherInfo getCityWeather(
            @ToolParam(description = "城市名称") String cityName,
            @ToolParam(description = "温度单位：C(摄氏度) 或 F(华氏度)", required = false) String unit) {

        LOGGER.info("MCP Tool getCityWeather called with city: {}, unit: {}", cityName, unit);

        // 模拟天气数据
        String temperature = "F".equalsIgnoreCase(unit) ? "77°F" : "25°C";
        String condition = "晴朗";

        return WeatherInfo.builder()
                .city(cityName)
                .temperature(temperature)
                .condition(condition)
                .humidity("65%")
                .wind("3级东南风")
                .advice("适合户外活动")
                .build();
    }

    /**
     * 工具3: 比较两个城市
     * 演示复杂参数的处理
     */
    @Tool(name = "compareCities",
            description = "比较两个城市的各项指标")
    public String compareCities(
            @ToolParam(description = "第一个城市名称") String city1,
            @ToolParam(description = "第二个城市名称") String city2) {

        LOGGER.info("MCP Tool compareCities called: {} vs {}", city1, city2);

        CityBaseInfo info1 = CITY_INFO_MAP.getOrDefault(city1,
                CityBaseInfo.builder().name(city1).population(0).area(0.0).build());
        CityBaseInfo info2 = CITY_INFO_MAP.getOrDefault(city2,
                CityBaseInfo.builder().name(city2).population(0).area(0.0).build());

        return String.format(
                "【%s vs %s 对比结果\n" +
                        "人口: %s %d 万 vs %s %d 万\n" +
                        "面积: %s %.2f km² vs %s %.2f km²\n" +
                        "结论: %s",
                city1, city2,
                city1, info1.getPopulation(), city2, info2.getPopulation(),
                city1, info1.getArea(), city2, info2.getArea(),
                info1.getPopulation() > info2.getPopulation() ? city1 + "人口更多" : city2 + "人口更多"
        );
    }

    @Tool(name = "theMostLivableCityInChina",description = "中国最宜居的城市")
    public String livingCity() {
        return "最宜居的城市是天津，风景秀美，美食众多，消费不贵。";
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        CITY_INFO_MAP.put("北京", CityBaseInfo.builder()
                .name("北京")
                .country("中国")
                .population(2189)
                .area(16410.54)
                .description("中国的首都，政治文化中心")
                .build());

        CITY_INFO_MAP.put("上海", CityBaseInfo.builder()
                .name("上海")
                .country("中国")
                .population(2487)
                .area(6340.5)
                .description("中国的经济金融中心，国际化大都市")
                .build());

        CITY_INFO_MAP.put("东京", CityBaseInfo.builder()
                .name("东京")
                .country("日本")
                .population(3740)
                .area(2194D)
                .description("日本的首都，世界最大的都市圈之一")
                .build());
    }
}
