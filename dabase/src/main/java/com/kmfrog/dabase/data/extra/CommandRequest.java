package com.kmfrog.dabase.data.extra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.kmfrog.dabase.AsyncObserver;
import com.kmfrog.dabase.Contract;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.util.UriUtils;

/**
 * 命令行异步请求封装。
 * @author dust@downjoy.com
 * @param <D>
 */
public class CommandRequest<D> extends Request<D, byte[]> {

    private final List<String> mCmdAndArgs;

    private final Class<? extends CommandOutputParser<D>> mParserClz;

    public CommandRequest(String cmd, AsyncObserver<D, Throwable> callback, String... args) {
        this(cmd, callback, null, args);
    }

    public CommandRequest(String cmd, AsyncObserver<D, Throwable> callback, Class<? extends CommandOutputParser<D>> parseClz,
        String... args) {
        super(UriUtils.build(Contract.URI_CMD_SCHEME, cmd, null), callback);
        setShouldCache(false);
        mParserClz=parseClz;
        mCmdAndArgs=new ArrayList<String>();
        mCmdAndArgs.add(cmd);
        if(args != null && args.length > 0) {
            mCmdAndArgs.addAll(Arrays.asList(args));
        }
    }

    @Override
    public String getUrl() {
        return null;
    }

    public List<String> getCmdAndArgs() {
        return mCmdAndArgs;
    }

    public Class<? extends CommandOutputParser<D>> getParserClz() {
        return mParserClz;
    }

}
