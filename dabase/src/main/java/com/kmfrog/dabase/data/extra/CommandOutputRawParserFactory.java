package com.kmfrog.dabase.data.extra;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.exception.ParserException;

public class CommandOutputRawParserFactory implements RawParser<Object, byte[]> {

    public static final String DEF_CHARSET="utf8";

    public static final String DEF_TYPE_IDENTITY="CLZ";

    @SuppressWarnings("rawtypes")
    protected static Map<Class<? extends CommandOutputParser>, CommandOutputParser> map=
        new ConcurrentHashMap<Class<? extends CommandOutputParser>, CommandOutputParser>();

    private static volatile boolean isRegisted=false;

    private static final CommandOutputRawParserFactory INSTANCE=new CommandOutputRawParserFactory();

    private CommandOutputRawParserFactory() {
        registerDefaultCommandOutputParser();
    }

    @SuppressWarnings("rawtypes")
    private CommandOutputRawParserFactory(List<CommandOutputParser> cmdOutputParsers) {
        this();
        registerCommandOutputParsers(cmdOutputParsers);
    }

    @SuppressWarnings("rawtypes")
    public void registerCommandOutputParsers(List<CommandOutputParser> cmdOutputParsers) {
        for(CommandOutputParser parser: cmdOutputParsers) {
            map.put(parser.getClass(), parser);
        }
        isRegisted=true;
    }

    private void registerDefaultCommandOutputParser() {
        map.put(CommandDummyParser.class, new CommandDummyParser());
    }

    /**
     * 获得命令行输出解析工厂类的实例。
     * @return
     */
    public synchronized static CommandOutputRawParserFactory getInstance() {
        return INSTANCE;
    }

    /**
     * 获得命令行输出解析工厂类的实例。
     * @return
     */
    @SuppressWarnings("rawtypes")
    public synchronized static CommandOutputRawParserFactory getInstance(List<CommandOutputParser> cmdOutputParsers) {
        if(!isRegisted) {
            INSTANCE.registerCommandOutputParsers(cmdOutputParsers);
        }
        return INSTANCE;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Object parse(byte[] raw, Map<String, Object> extra) throws ParserException {
        String charset=(String)extra.get("charset");
        Class<? extends CommandOutputParser> clz=(Class<? extends CommandOutputParser>)extra.get("CommandOutputParser");
        try {
            String output=new String(raw, charset == null ? DEF_CHARSET : charset);
            CommandOutputParser parser=map.get(clz == null ? CommandDummyParser.class : clz);
            if(parser == null) {
                if(DLog.DEBUG) {
                    DLog.d(new StringBuilder().append("unexpect CommandOutputParser:").append(clz.getName()).toString());
                }
                return null;
            }
            return parser.parseObject(output);
        } catch(Exception ex) {
            throw new ParserException(ex);
        }

    }

}
