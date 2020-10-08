package de.greenlood.alsatian.webstorm.plugin.rest;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.CharsetToolkit;
import de.greenlood.alsatian.webstorm.plugin.AlsatianOutputToGeneralTestEventsConverter;
import io.netty.buffer.ByteBufInputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.HttpRequestHandler;
import org.jetbrains.io.Responses;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class AlsatianTestExecutionListener extends HttpRequestHandler {
    private Gson gson = new Gson();
    protected static final Logger LOG = Logger.getInstance(AlsatianTestExecutionListener.class);

    @Override
    public boolean isAccessible(@NotNull HttpRequest request) {
        return true;
    }

    public final boolean isSupported(@NotNull FullHttpRequest request) {
        if (!this.isMethodSupported(request.method())) {
            return false;
        } else {
            String uri = request.uri();
            if (this.isPrefixlessAllowed() && checkPrefix(uri, this.getServiceName())) {
                return true;
            } else {
                String serviceName = this.getServiceName();
                int minLength = 1 + "api".length() + 1 + serviceName.length();
                if (uri.length() >= minLength && uri.charAt(0) == '/' && uri.regionMatches(true, 1, "api", 0, "api".length()) && uri.regionMatches(true, 2 + "api".length(), serviceName, 0, serviceName.length())) {
                    if (uri.length() == minLength) {
                        return true;
                    } else {
                        char c = uri.charAt(minLength);
                        return c == '/' || c == '?';
                    }
                } else {
                    return false;
                }
            }
        }
    }


    @Override
    public boolean process(@NotNull QueryStringDecoder queryStringDecoder, @NotNull FullHttpRequest request, @NotNull ChannelHandlerContext context) throws IOException {

        try {
            if (!this.isHostTrusted(request)) {
                Responses.send(HttpResponseStatus.FORBIDDEN, context.channel(), request);
                return true;
            }
            //debugRestCall(queryStringDecoder);
            if (queryStringDecoder.path().endsWith("logs")) {

            } else if (queryStringDecoder.path().endsWith("warnings")) {

            } else if (queryStringDecoder.path().endsWith("teststarted")) {
                String testName = queryStringDecoder.parameters().get("test").get(0);
                String fixtureName = queryStringDecoder.parameters().get("fixture").get(0);
                TestStartedRequest testStartedRequest = readEntityFromStream(request, TestStartedRequest.class);
                AlsatianOutputToGeneralTestEventsConverter.instance.onTestStarted(new TestStartedEvent(testName, "test://" + testStartedRequest.filePath, fixtureName + ":" + testName));
            } else if (queryStringDecoder.path().endsWith("testfixturestarted")) {
                String fixtureName = queryStringDecoder.parameters().get("fixture").get(0);
                TestStartedRequest testStartedRequest = readEntityFromStream(request, TestStartedRequest.class);
                AlsatianOutputToGeneralTestEventsConverter.instance.onSuiteStarted(new TestSuiteStartedEvent(fixtureName, testStartedRequest.filePath, fixtureName));
            } else if (queryStringDecoder.path().endsWith("testingFinished")) {
                AlsatianOutputToGeneralTestEventsConverter.instance.onFinishTesting();

            } else if (queryStringDecoder.path().endsWith("testfixtureresult")) {
                String fixtureName = queryStringDecoder.parameters().get("fixture").get(0);
                AlsatianOutputToGeneralTestEventsConverter.instance.onSuiteFinished(new TestSuiteFinishedEvent(fixtureName));
            } else if (queryStringDecoder.path().endsWith("testresult")) {
                String testName = queryStringDecoder.parameters().get("test").get(0);
                String fixtureName = queryStringDecoder.parameters().get("fixture").get(0);

                TestResultRequest testResultRequest = readEntityFromStream(request, TestResultRequest.class);
                TestOutcome testOutcome = TestOutcome.fromNumber(testResultRequest.getOutcome());
                /*
                //todo use gson?
                JsonReader reader = createJsonReader(request);
                reader.beginObject();

                TestOutcome testOutcome = null;
                Long executionTimeMilliseconds = null;
                String ignoreReason = null;
                JSError jsError = null;
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    if (name.equals("executionTime")) {
                        executionTimeMilliseconds = reader.nextLong();
                    } else if (name.equals("outcome")) {
                        int testResultCode = reader.nextInt();
                        testOutcome = TestOutcome.fromNumber(testResultCode);
                    } else if (name.equals("error")) {
                        if (reader.peek() == JsonToken.NULL)
                            reader.skipValue();
                        else {
                            jsError = parseError(reader);
                        }
                    } else if (name.equals("logs")) {
                        reader.beginArray();
                        while (reader.hasNext()) {
                            String log = reader.nextString();
                            throw new UnsupportedOperationException(log);

                        }
                        reader.endArray();
                    } else if (name.equals("ignoreReason")) {
                        ignoreReason = reader.nextString();
                    }
                }
                reader.endObject();

                 */
                if (testOutcome == TestOutcome.Pass) {
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestFinished(new TestFinishedEvent(testName, testResultRequest.getExecutionTime()));
                } else if (testOutcome == TestOutcome.Skip) {
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestIgnored(new TestIgnoredEvent(testName, testResultRequest.getIgnoreReason(), null));
                } else if (testOutcome == TestOutcome.Fail) {
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestFailure(new TestFailedEvent(testName, "", null, true, null, null));
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestFinished(new TestFinishedEvent(testName, testResultRequest.getExecutionTime()));
                } else if (testOutcome == TestOutcome.Error) {
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestFailure(new TestFailedEvent(testName, testResultRequest.getError().getMessage(), testResultRequest.getError().getStack(), true, null, null));
                    AlsatianOutputToGeneralTestEventsConverter.instance.onTestFinished(new TestFinishedEvent(testName, testResultRequest.getExecutionTime()));
                }
            } else if (queryStringDecoder.path().contains("console")) {
                String testName = queryStringDecoder.parameters().get("test").get(0);
                String text = request.content().toString(Charset.forName("UTF-8"));
                AlsatianOutputToGeneralTestEventsConverter.instance.onTestOutput(new TestOutputEvent(testName, text + System.lineSeparator(), true));
            }
            Responses.send(HttpResponseStatus.OK, context.channel(), request);
            return true;
        } catch (Exception e) {
            LOG.error(e);
            Responses.send(HttpResponseStatus.INTERNAL_SERVER_ERROR, context.channel(), request);
            return false;
        }
    }

    private <T> T readEntityFromStream(@NotNull FullHttpRequest request, Class<T> typeOfT) throws IOException {
        try (JsonReader reader = createJsonReader(request)) {
            Gson gson = new Gson();
            T entity = gson.fromJson(reader, typeOfT);
            return entity;
        }
    }

    private void debugRestCall(@NotNull QueryStringDecoder queryStringDecoder) {
        List<String> fixtures = queryStringDecoder.parameters().get("fixture");
        String fixtureName = "";
        if (fixtures != null)
            fixtureName = fixtures.get(0);
        List<String> testParameters = queryStringDecoder.parameters().get("test");
        String testName = "";
        if (testParameters != null) {
            testName = testParameters.get(0);
        }
        System.out.println("\n\n!!!!" + queryStringDecoder.path() + " " + fixtureName + " " + testName + "\n\n");
    }

    @NotNull
    protected String getServiceName() {
        return "alsatiantestexecutionlistener";
    }

    protected boolean isMethodSupported(@NotNull HttpMethod httpMethod) {
        return httpMethod == HttpMethod.GET || httpMethod == HttpMethod.POST;
    }

    protected boolean isPrefixlessAllowed() {
        return false;
    }


    private JSError parseError(JsonReader reader) throws IOException {
        return gson.fromJson(reader, JSError.class);

    }

    private static List<AlsatianTestMethod> readTests(JsonReader reader) throws IOException {
        List<AlsatianTestMethod> tests = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            tests.add(readerTest(reader));
        }
        reader.endArray();
        return tests;
    }

    private static AlsatianTestMethod readerTest(JsonReader reader) throws IOException {
        String key = null;
        String description = null;
        boolean ignored = true;
        boolean focussed = false;
        long timeout = 0;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("key"))
                key = reader.nextString();
            else if (name.equals("description"))
                description = reader.nextString();
            else if (name.equals("ignored"))
                ignored = reader.nextBoolean();

            else if (name.equals("focussed"))
                focussed = reader.nextBoolean();

            else if (name.equals("timeout")) {
                if (reader.peek() == JsonToken.NULL)
                    reader.skipValue();
                else
                    timeout = reader.nextLong();
            } else if (name.equals("testCases"))
                reader.skipValue();
            else
                throw new IllegalStateException("Unknown element " + name);

        }
        reader.endObject();
        return new AlsatianTestMethod(key, description, ignored, focussed, timeout);
    }

    protected boolean isHostTrusted(@NotNull FullHttpRequest request) throws InterruptedException, InvocationTargetException {
        return true;
    }

    @NotNull
    protected static JsonReader createJsonReader(@NotNull FullHttpRequest request) {

        JsonReader reader = new JsonReader(new InputStreamReader(new ByteBufInputStream(request.content()), CharsetToolkit.UTF8_CHARSET));
        reader.setLenient(true);
        return reader;
    }


}