/* <copyright>
 * Copyright (C) 2022 Janusch Rentenatus & Thomas Weber 
 * Copyright (c) 2025, Janusch Rentenatus. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * </copyright>
 */
package de.jare.jsoncasted.parserservice;

import de.jare.debug.JsonDebugLevel;
import de.jare.jsoncasted.lang.JsonNode;
import de.jare.jsoncasted.lang.JsonResource;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderTinkerResult;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderTinker;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderScanResult;
import de.jare.jsoncasted.lang.calculator.JsonWoodProviderScanner;
import de.jare.jsoncasted.parserwriter.JsonParseException;
import java.io.IOException;

/**
 *
 * @author Janusch Rentenatus
 *
 */
public class RootParser {

    public static JsonResource parse(ParseStreamReader psr, JsonResource container, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
        final JsonNode rootNode = parseRoot(psr);
        container.setRoot(rootNode);
        JsonWoodProviderScanResult scan = JsonWoodProviderScanner.INSTANCE.scan(rootNode);
        JsonWoodProviderTinkerResult result = JsonWoodProviderTinker.INSTANCE.build(scan);
        container.setLinkingSet(WoodIdFinder.buildLinkingSet(rootNode, container.getProviderName(), debugLevel));
        if (result.hasExceptions()) {
            container.addExceptions(result.getExceptions());
        }
        container.setExpectedBox(result.getWoodProviderBox());
        return container;
    }

    static JsonNode parseRoot(ParseStreamReader psr) throws IOException, JsonParseException {
        StringBuilder sb = new StringBuilder();
        while (psr.hasNext()) {
            char c = psr.next();
            if (c == '{') {
                return ObjectParser.parse(psr);
            }
            if (c == '[') {
                return ListParser.parse(psr);
            }
            if (c == '"') {
                return JsonNode.stringNode(StringParser.parse(psr, '"'));
            }
            if (c == '\'') {
                return JsonNode.stringNode(StringParser.parse(psr, '\''));
            }
            if (c == '(') {
                return CastingParser.parse(psr);
            }
            sb.append(c);
        }
        return JsonNode.varNode(sb.toString());
    }

}
