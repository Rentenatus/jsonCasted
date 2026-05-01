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
 * Root parser for JSON content that coordinates the parsing process.
 *
 * <p>This class handles the initial parsing of JSON content and coordinates
 * with other parsers ({@link ObjectParser}, {@link ListParser}, {@link StringParser},
 * {@link CastingParser}) to build the complete JSON tree.</p>
 *
 * <p>After parsing the root node, it also:</p>
 * <ul>
 *   <li>Scans for wood provider definitions</li>
 *   <li>Builds the linking set for cross-resource references</li>
 *   <li>Processes and stores any exceptions from the scanning/building process</li>
 * </ul>
 *
 * @author Janusch Rentenatus
 *
 */
public class RootParser {

    /**
     * Parses JSON content from the stream reader into the specified container.
     *
     * <p>This method performs the complete parsing pipeline including:</p>
     * <ol>
     *   <li>Root node parsing</li>
     *   <li>Wood provider scanning</li>
     *   <li>Linking set construction</li>
     *   <li>Exception collection</li>
     * </ol>
     *
     * @param psr the ParseStreamReader providing character input.
     * @param container the JsonResource to populate with parsed content.
     * @param debugLevel the debug level for logging.
     * @return the populated JsonResource with root node and metadata.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
    public static JsonResource parse(ParseStreamReader psr, JsonResource container, JsonDebugLevel debugLevel) throws IOException, JsonParseException {
        final JsonNode rootNode = parseRoot(psr);
        container.setRoot(rootNode);
        JsonWoodProviderScanResult scan = JsonWoodProviderScanner.INSTANCE.scan(rootNode);
        JsonWoodProviderTinkerResult result = JsonWoodProviderTinker.INSTANCE.build(scan, debugLevel);
        container.setLinkingSet(WoodIdFinder.buildLinkingSet(rootNode, container.getProviderName(), debugLevel));
        if (result.hasExceptions()) {
            container.addExceptions(result.getExceptions());
        }
        container.setExpectedBox(result.getWoodProviderBox());
        return container;
    }

    /**
     * Parses the root JSON value from the stream.
     *
     * <p>Recognizes and delegates to appropriate parsers based on the first character:</p>
     * <ul>
     *   <li>'{' - delegates to {@link ObjectParser}</li>
     *   <li>'[' - delegates to {@link ListParser}</li>
     *   <li>"'" or '"' - parses as string</li>
     *   <li>'(' - delegates to {@link CastingParser} for type-cast syntax</li>
     *   <li>otherwise - accumulates characters as a variable value</li>
     * </ul>
     *
     * @param psr the ParseStreamReader providing character input.
     * @return the parsed JsonNode root.
     * @throws IOException if an I/O error occurs.
     * @throws JsonParseException if JSON parsing fails.
     */
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
