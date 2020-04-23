package com.pathaks.splitaggregate.routes;

import com.pathaks.splitaggregate.aggregators.JSONAggregator;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class MainRoute extends RouteBuilder{

    @Override
    public void configure() throws Exception {
       
        from("file:data/in?readLock=markerFile")
        .log("File has been picked and sent to Split")
        .wireTap("log:INFO?showHeaders=true&showProperties=true&multiline=true")
        .split(body().tokenizeXML("doc","response")).streaming()
            .to("seda:fromSplit?size=70000")   
        .end()
        .log("File has been completely split.");


        from("seda:fromSplit?size=70000")
            .setHeader("ID", xpath("//str[@name='ProductId']/text()"))
            .wireTap("log:INFO?showHeaders=true&showProperties=true&multiline=true&showBody=true")
            .wireTap("file:data/out/XML?fileName=${header.ID}.xml")
            .to("seda:convertToJson?size=70000");

        from("seda:convertToJson?size=70000")
            .to("xj:?transformDirection=XML2JSON")
            .wireTap("log:INFO?showHeaders=true&showProperties=true&multiline=true&showBody=true")
            .wireTap("file:data/out/JSON?fileName=${header.ID}.json")
            .aggregate(constant(true), new JSONAggregator())
                .completionSize(500)
                .completionTimeout(3000)
                .convertBodyTo(String.class)
                .wireTap("file:data/out/Aggregated/?fileName=${date:now:yyyy/MM/dd/HH-mm-ss}.json")
                .wireTap("log:INFO?showHeaders=true&showProperties=true&multiline=true")
            .end();  
    }

}