package dev.amritanka.assistant.tools;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

/**
 * Example function-callable tool. Spring AI auto-derives the JSON schema
 * from the input record type, exposes it to the model, and dispatches
 * invocations back to this bean.
 */
@Component("getWeather")
@Description("Get current weather for a city. Use when the user asks about weather.")
public class WeatherTool implements Function<WeatherTool.Request, WeatherTool.Response> {

    public record Request(String city) {}
    public record Response(String city, String summary, double tempC) {}

    @Override
    public Response apply(Request req) {
        // Stub — wire to a real provider (OpenWeather, Azure Maps, ...) for production.
        return new Response(req.city(), "Partly cloudy", 22.5);
    }
}
