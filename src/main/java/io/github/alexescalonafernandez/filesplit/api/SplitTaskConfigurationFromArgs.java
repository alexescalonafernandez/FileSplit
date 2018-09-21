package io.github.alexescalonafernandez.filesplit.api;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static io.github.alexescalonafernandez.filesplit.api.utils.IntrospectionUtil.*;
import static io.github.alexescalonafernandez.filesplit.api.utils.SplitTaskConfigurationUtils.getRequiredMethods;

/**
 * Created by alexander.escalona on 19/09/2018.
 */
public class SplitTaskConfigurationFromArgs implements SplitTaskConfiguration{

    private class Data {
        Object value;
        boolean isDefault;

        public Data(Object value, boolean isDefault) {
            this.value = value;
            this.isDefault = isDefault;
        }
    }

    private final HashMap<Method, Data> store;
    private final ArgumentValues argumentValues;
    private final DefaultValues defaultValues;
    private final SplitTaskConfiguration proxy;
    public SplitTaskConfigurationFromArgs(String[] args) {
        store = new HashMap<>();
        argumentValues = new ArgumentValues(args);
        defaultValues = new DefaultValues();
        proxy = (SplitTaskConfiguration) Proxy.newProxyInstance(
                SplitTaskConfiguration.class.getClassLoader(),
                new Class[]{SplitTaskConfiguration.class},
                (proxy, method, methodArgs) -> {
                    Method interfaceMethod = getInterfaceMethodFrom(method);
                    return Optional.ofNullable(store.get(interfaceMethod))
                            .map(data -> (Supplier<Data>) () -> data)
                            .orElse(() -> {
                                Data data = Optional.ofNullable(getMethodValue(argumentValues, interfaceMethod, methodArgs))
                                        .map(value ->  new Data(value, false))
                                        .orElse(new Data(getMethodValue(defaultValues, interfaceMethod, methodArgs), true));
                                store.put(interfaceMethod, data);
                                return data;
                            })
                            .get().value;
                }
        );

        //populate store
        getRequiredMethods()
                .forEach(method -> {
                    try {
                        method.invoke(proxy);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
        proxy.getMaxThreadNumber(8);
    }

    private Method getInterfaceMethodFrom(Method method) {
        return Arrays.asList(SplitTaskConfiguration.class.getMethods())
                .stream()
                .filter(m ->
                        hasReturnTypeOf(method.getReturnType(), m) &&
                                hasSameNameAndParameters(method, m, false)
                ).findFirst()
                .orElse(null);
    }

    public boolean isArgumentDataDefault(Method method) {
        final Method interfaceMethod = getInterfaceMethodFrom(method);
        return Optional.ofNullable(interfaceMethod)
                .map(m -> store.get(m))
                .map(data -> data.isDefault)
                .orElse(true);
    }

    @Override
    public OperationMode getOperationMode() {
        return proxy.getOperationMode();
    }

    @Override
    public String getFilePath() {
        return proxy.getFilePath();
    }

    @Override
    public Long getChunkSize() {
        return proxy.getChunkSize();
    }

    @Override
    public Boolean appendFirstLine() {
        return proxy.appendFirstLine();
    }

    @Override
    public String getFolderPath() {
        return proxy.getFolderPath();
    }

    @Override
    public Integer getThreadNumber() {
        return proxy.getThreadNumber();
    }

    @Override
    public Integer getMaxThreadNumber(int limit) {
        return proxy.getMaxThreadNumber(limit);
    }

    @Override
    public Integer getMaxLines() {
        return proxy.getMaxLines();
    }

    @Override
    public String getPrefix() {
        return proxy.getPrefix();
    }

    @Override
    public String getRegex() {
        return proxy.getRegex();
    }

    @Override
    public Integer getRegexGroup() {
        return proxy.getRegexGroup();
    }


    private class ArgumentValues implements SplitTaskConfiguration {
        private final HashMap<String, String> arguments;

        private ArgumentValues(String[] args) {
            arguments = new HashMap<>();
            for(int i = 0; i < args.length - 1; i+=2) {
                arguments.put(args[i], args[i + 1]);
            }
        }

        private String getArgumentNameFromEnclosingMethod(Object runtimeObject) {
            final Method currentMethod = getCurrentEnclosingMethodFrom(runtimeObject);
            final Method interfaceMethod = getInterfaceMethodFrom(currentMethod);
            return getRequiredMethods()
                    .filter(method -> method.equals(interfaceMethod))
                    .findFirst()
                    .filter(method -> method.getAnnotation(Required.class) != null)
                    .map(method -> method.getAnnotation(Required.class).argumentName())
                    .orElse(null);
        }


        @Override
        public OperationMode getOperationMode() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .map(value -> {
                        try {
                            return OperationMode.valueOf(value.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            return null;
                        }
                    })
                    .filter(mode -> !OperationMode.ANY.equals(mode))
                    .orElse(null);
        }

        @Override
        public String getFilePath() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(filePath -> {
                        File file = new File(filePath);
                        return file.exists() && file.isFile();
                    })
                    .orElse(null);
        }

        @Override
        public Long getChunkSize() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(s -> {
                        try {
                            long value = Long.valueOf(s);
                            long oneGB = (long)Math.pow(1024, 3);
                            return value > 0 && value <= oneGB;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }).map(Long::valueOf)
                    .orElse(null);
        }

        @Override
        public Boolean appendFirstLine() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .map(Boolean::valueOf)
                    .orElse(null);
        }

        @Override
        public String getFolderPath() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(filePath -> {
                        File file = new File(filePath);
                        if(file.exists() && file.isDirectory()){
                            return true;
                        } else {
                            try {
                                FileUtils.forceMkdir(file);
                                return true;
                            } catch (IOException e) {
                                return false;
                            }
                        }
                    })
                    .orElse(null);
        }

        @Override
        public Integer getThreadNumber() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(s -> {
                        try {
                            int value = Integer.valueOf(s);
                            return value > 0;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .map(Integer::valueOf)
                    .orElse(null);
        }

        @Override
        public Integer getMaxThreadNumber(int limit) {
            return getThreadNumber();
        }

        @Override
        public Integer getMaxLines() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(s -> {
                        try {
                            int value = Integer.valueOf(s);
                            return value > 0 && value <= 1000000;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .map(Integer::valueOf)
                    .orElse(null);
        }

        @Override
        public String getPrefix() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(prefix -> prefix.trim().length() > 0)
                    .orElse(null);
        }

        @Override
        public String getRegex() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(regex -> {
                        try {
                            Pattern.compile(regex);
                            return true;
                        } catch (PatternSyntaxException ex) {
                            return false;
                        }
                    })
                    .orElse(null);
        }

        @Override
        public Integer getRegexGroup() {
            return Optional.ofNullable(getArgumentNameFromEnclosingMethod(new Object(){}))
                    .map(argumentName -> arguments.get(argumentName))
                    .filter(s -> {
                        try {
                            int value = Integer.valueOf(s);
                            return value > 0 && value < 10;
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    })
                    .map(Integer::valueOf)
                    .orElse(null);
        }
    }


    private class DefaultValues implements SplitTaskConfiguration {

        @Override
        public OperationMode getOperationMode() {
            return null;
        }

        @Override
        public String getFilePath() {
            return null;
        }

        @Override
        public Long getChunkSize() {
            long oneMB = 1024 * 1024;
            return oneMB;
        }

        @Override
        public Boolean appendFirstLine() {
            return false;
        }

        @Override
        public String getFolderPath() {
            return null;
        }

        @Override
        public Integer getThreadNumber() {
            return 8;
        }

        @Override
        public Integer getMaxThreadNumber(int limit) {
            return getThreadNumber();
        }

        @Override
        public Integer getMaxLines() {
            return 100000;
        }

        @Override
        public String getPrefix() {
            return null;
        }

        @Override
        public String getRegex() {
            return null;
        }

        @Override
        public Integer getRegexGroup() {
            return null;
        }
    }
}
