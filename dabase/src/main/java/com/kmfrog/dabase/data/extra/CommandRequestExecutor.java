package com.kmfrog.dabase.data.extra;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import com.kmfrog.dabase.DLog;
import com.kmfrog.dabase.data.Cache;
import com.kmfrog.dabase.data.RawParser;
import com.kmfrog.dabase.data.Request;
import com.kmfrog.dabase.data.RequestExecutor;
import com.kmfrog.dabase.data.Response;
import com.kmfrog.dabase.exception.BaseException;

public class CommandRequestExecutor extends RequestExecutor<Object, byte[]> {

    public CommandRequestExecutor(RawParser<Object, byte[]> parser, Cache cache) {
        super(parser, cache);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Response<Object> exec(Request<Object, byte[]> req) throws BaseException {

        CommandRequest request=(CommandRequest)req;
        ByteArrayOutputStream byteArrayOut=new ByteArrayOutputStream();
        Map<String, Object> extras=new HashMap<String, Object>();
        try {
            Process process=new ProcessBuilder().command(request.getCmdAndArgs()).redirectErrorStream(true).start();
            // Process process=new
            // ProcessBuilder().command("/system/bin/ping","-c 4","211.147.5.151").redirectErrorStream(true).start();

            try {
                InputStream in=process.getInputStream();
                OutputStream out=process.getOutputStream();

                byte[] buffer=new byte[1024];
                int len=0;
                while((len=in.read(buffer)) > -1) {
                    // Log.i("Ping", new String(buffer, 0, count, "utf8"));
                    byteArrayOut.write(buffer, 0, len);
                }
                int exitCode=process.waitFor();
                out.close();
                in.close();
                extras.put("exitCode", exitCode);
                extras.put("CommandOutputParser", request.getParserClz());
            } finally {
                process.destroy();
                process=null;
            }
            final RawParser<Object, byte[]> parser=getParser();
            Object data=parser.parse(byteArrayOut.toByteArray(), extras);
            request.addMarker("data-parse-complete");
            return new Response<Object>(data, false);
        } catch(Throwable ex) {
            if(DLog.DEBUG) {
                DLog.e("CommandRequestExecutor.exec", ex);
            }
            return new Response<Object>(new BaseException(ex));
        }
    }

}
