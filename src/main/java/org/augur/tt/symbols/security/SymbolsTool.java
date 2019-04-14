package org.augur.tt.symbols.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;

public class SymbolsTool
//        extends AbstractShell 
{
//
//    public static void main(final String[] args) throws Throwable {
//        LogManager.getLogManager().readConfiguration(new ByteArrayInputStream("handlers=java.util.logging.ConsoleHandler\njava.util.logging.ConsoleHandler.level=ALL\njava.util.logging.ConsoleHandler.formatter = deltix.util.log.TerseFormatter\ndeltix.qsrv.hf.plugins.data.level=FINEST\n.level=INFO\n".getBytes()));
//        new SymbolsTool(args).run();
////        tests();
//    }
//
//    static void tests() throws IOException {
//
//        final UTCTimestamp sendingTime = new UTCTimestamp();
//        final String password = "12345678";
//
//        while (true) {
//
//            try (final Socket socket = new Socket()) {
//
//                socket.setTcpNoDelay(false);
////                socket.setKeepAlive(true);
////                socket.setTrafficClass(4);
//                socket.connect(new InetSocketAddress("fixsecurityinfo-ext-uat-cert.trade.tt", 11503), 0);
//                final OutputStream output = socket.getOutputStream();
//                final InputStream input = socket.getInputStream();
//
//                try {
//                    final OutputFIXMessage msg = new OutputFIXMessage("FIX.4.2", "A");
//                    final OutputFIXMessage.Part header = msg.getHeader();
//                    final OutputFIXMessage.Part body = msg.getBody();
////                    8=FIX.4.2;9=112;35=A;49=ENNK_MD;56=CME;50=NKowalski;57=70;52=20190326-13:27:49.413;34=1;
//                    header.addField(49, "ENNK_MD");
//                    header.addField(56, "CME");
//                    header.addField(50, "NKowalski");
////                    header.addField(57, "70");
//                    header.addField(52, sendingTime.now().toText()).addField(34, "1");
////                    98=0;108=30;141=Y;95=8;96=12345678
//                    body.addField(98, "0");
//                    body.addField(FIXField.HeartBtInt, "30");
//                    body.addField(FIXField.ResetSeqNumFlag, "Y");
//                    body.addField(95, password.length());
//                    body.addField(96, password);
//
//                    final String msgStr = msg.toString();
//                    System.out.println(">>> " + DebugUtil.formatFixMessage(msgStr));
//                    msg.write(output);
//                    final int length = msgStr.length() - "8=FIX.4.2;9=102".length() - "10=XXX;".length();
//                    System.out.println("sum=" + MessageUtils.checksum(msgStr) + ", length=" + length);
//
//                    final InputStreamParser parser = new InputStreamParser(input);
//                    final InputFIXMessage in = new InputFIXMessage();
//                    while (parser.next(in)) {
//                        System.out.println("<<< " + DebugUtil.formatFixMessage(in.toString()));
//                    }
//
//                    System.out.println("SESSION END");
//
//                } catch (Exception ex) {
//                    System.err.println("2> " + ex.getMessage());
//                } finally {
//                    try {
//                        input.close();
//                    } finally {
//                        try {
//                            output.flush();
//                        } finally {
//                            try {
//                                output.close();
//                            } finally {
//                                socket.close();
//                            }
//                        }
//                    }
//                }
//            } catch (Exception ex) {
//                System.err.println("1> " + ex.getMessage());
//            }
//            System.in.read();
//        }
//    }
//
//    private final String brokerID = "TT";
//    private final TTConfig config;
//    private final SecurityRequestParamsList params = new SecurityRequestParamsList();
//
//    private Walker walker = null;
//    private String tbUrl = "dxtick://127.0.0.1:8010";
//
//    public SymbolsTool(final String[] args) {
//        super(args);
//        this.config = new TTConfig();
//        config.setConnectionTimeout(3000);
//        this.log("***************           Trading Technologies Symbols Tool          ***************");
//        this.log("");
//    }
//
//    @Override
//    protected boolean doCommand(final String key, final String args) throws Exception {
//        final String lowerCase = key.toLowerCase();
//        switch (lowerCase) {
//            case "show": {
//                show();
//                return true;
//            }
//            case "add": {
//                addSecReq(args);
//                return true;
//            }
//            case "send": {
//                send(args);
//                return true;
//            }
//            case "load": {
//                load(args);
//                return true;
//            }
//            case "save": {
//                save(args);
//                return true;
//            }
//            case "clear": {
//                params.clear();
//                return true;
//            }
//            case "disconnect": {
//                if (walker != null) {
//                    walker.stopSession();
//                } else {
//                    log("No session started");
//                }
//                return true;
//            }
//            default: {
//                return super.doCommand(key, args);
//            }
//        }
//    }
//
//    private void save(final String args) {
//        try {
//            this.params.save(args);
//        } catch (Exception ex) {
//            log("Error save " + args + " : " + ex.getMessage());
//        }
//    }
//
//    private void load(final String args) {
//        try {
//            this.params.load(args);
//        } catch (Exception ex) {
//            log("Error load " + args + " : " + ex.getMessage());
//        }
//    }
//
//    @Override
//    protected boolean doSet(final String option, final String value) throws Exception {
//        switch (option) {
//            case "host": {
//                this.config.setHost(value);
//                return true;
//            }
//            case "port": {
//                final int port = Integer.parseInt(value);
//                this.config.setPort(port);
//                return true;
//            }
//            case "begin_string": {
//                this.config.setBeginString(value);
//                return true;
//            }
//            case "sender_comp_id": {
//                this.config.setSenderCompID(value);
//                return true;
//            }
//            case "target_comp_id": {
//                this.config.setTargetCompID(value);
//                return true;
//            }
//            case "sender_sub_id": {
//                this.config.setSenderSubID(value);
//                return true;
//            }
//            case "target_sub_id": {
//                this.config.setTargetSubID(value);
//                return true;
//            }
//            case "password": {
//                this.config.setPassword(value);
//                return true;
//            }
//            case "fix_log_level": {
//                this.config.getLogger().setLevel(Level.parse(value));
//                return true;
//            }
//            case "id_source": {
//                SecurityIDSource sis = null;
//                try {
//                    sis = SecurityIDSource.valueOf(value);
//                } catch (IllegalArgumentException ex) {
//                }
//                if (sis == null) {
//                    sis = SecurityIDSource.fromCode(value);
//                }
//                if (sis != null) {
//                    this.config.setSecIdSrc(sis);
//                }
//                return true;
//            }
//            case "tburl": {
//                this.tbUrl = value;
//                return true;
//            }
//            default: {
//                return super.doSet(option, value);
//            }
//        }
//    }
//
//    private void show() {
//        log("Registered requests: " + params.size());
//        final Iterator<SecurityRequestParamsList.SecurityRequestParams> iterator = params.iterator();
//        final StringBuilder sb = new StringBuilder();
//        while (iterator.hasNext()) {
//            final SecurityRequestParamsList.SecurityRequestParams srp = iterator.next();
//            sb.append("- ").append(srp);
//            if (iterator.hasNext()) {
//                sb.append(System.lineSeparator());
//            }
//        }
//        log(sb);
//    }
//
//    @Override
//    protected void doSet() {
//        this.log("Trading Technologies Connection Options:");
//        this.log("  host           : " + this.config.getHost());
//        this.log("  port           : " + this.config.getPort());
//        this.log("  begin_string   : " + this.config.getBeginString());
//        this.log("  sender_comp_id : " + this.config.getSenderCompID());
//        this.log("  target_comp_id : " + this.config.getTargetCompID());
//        this.log("  sender_sub_id  : " + this.config.getSenderSubID());
//        this.log("  target_sub_id  : " + this.config.getTargetSubID());
//        this.log("  id_source      : " + this.config.getSecIdSrc());
//        this.log("  tburl          : " + this.tbUrl);
//        this.log("  password       : " + (this.config.getPassword() != null && !this.config.getPassword().isEmpty()));
//        this.log("  fix_log_level  : " + this.config.getLogger().getLevel());
//    }
//
//    private void addSecReq(final String args) {
//        final Set<String> symbols = new HashSet<>();
//        final Set<String> secIds = new HashSet<>();
//        String secType = null;
//        String secExchange = null;
//        String exDestination = null;
//        SecurityRequestType reqType = SecurityRequestType.LIST;
//        boolean tickTable = false;
//        final String[] pairs = args.split(";");
//        for (int i = 0; i < pairs.length; i++) {
//            final String[] pair = pairs[i].split("=");
//            if (pair.length != 2) {
//                throw new IllegalArgumentException("Invalid pair format(tag=value): " + pair);
//            }
//            final int tag = Integer.parseInt(pair[0]);
//            final String value = pair[1];
//            switch (tag) {
//                case FIXField.Symbol: {
//                    final String[] values = value.split(",");
//                    symbols.addAll(Arrays.asList(values));
//                    break;
//                }
//                case FIXField.SecurityID: {
//                    final String[] values = value.split(",");
//                    secIds.addAll(Arrays.asList(values));
//                    break;
//                }
//                case FIXField.SecurityType: {
//                    secType = value;
//                    break;
//                }
//                case FIXField.SecurityExchange: {
//                    secExchange = value;
//                    break;
//                }
//                case 100: {
//                    exDestination = value;
//                    break;
//                }
//                case 321: {
//                    SecurityRequestType srt = null;
//                    try {
//                        srt = SecurityRequestType.valueOf(value);
//                    } catch (IllegalArgumentException ex) {
////                        log(ex.getMessage());
//                    }
//                    if (srt == null) {
//                        Integer code = null;
//                        try {
//                            code = Integer.valueOf(value);
//                        } catch (IllegalArgumentException ex) {
////                            log(ex.getMessage());
//                        }
//                        srt = SecurityRequestType.fromCode(code);
//                    }
//                    reqType = srt;
//                    break;
//                }
//                case 17000: {
//                    tickTable = Boolean.parseBoolean(value);
//                    break;
//                }
//            }
//        }
//        if (!secIds.isEmpty()) {
//            for (String secId : secIds) {
//                final SecurityRequestParamsList.SecurityRequestParams param = params.addSecurityGroup(null, null, tickTable, secId, secType, null, reqType);
//                log("Added: " + param);
//            }
//        } else {
//            if (symbols.isEmpty()) {
//                log("ERROR: Add security request fail: Symbol(#55) missing");
//                return;
//            }
//            if (secType == null) {
//                log("ERROR: Add security request fail: SecurityType(#167) missing");
//                return;
//            }
//            if (secExchange == null && exDestination == null) {
//                log("ERROR: Add security request fail: SecurityExchange(#55) or ExDestination(#100) missing");
//                return;
//            }
//            for (String symbol : symbols) {
//                final SecurityRequestParamsList.SecurityRequestParams param = params.addSecurityGroup(exDestination, secExchange, tickTable, null, secType, symbol, reqType);
//                log("Added: " + param);
//            }
//        }
//    }
//
//    private void send(final String args) throws Exception {
//        if (this.config.getHost() == null || this.config.getHost().length() == 0) {
//            this.log("host option is required");
//            return;
//        }
//        if (this.config.getPort() <= 0) {
//            this.log("port option is required");
//            return;
//        }
//        if (this.config.getBeginString() == null || this.config.getBeginString().length() == 0) {
//            this.log("begin_string option is required");
//            return;
//        }
//        if (this.config.getSenderCompID() == null || this.config.getSenderCompID().length() == 0) {
//            this.log("sender_comp_id option is required");
//            return;
//        }
//        if (this.config.getTargetCompID() == null || this.config.getTargetCompID().length() == 0) {
//            this.log("target_comp_id option is required");
//            return;
//        }
//        if (this.config.getSenderSubID() == null || this.config.getSenderSubID().length() == 0) {
//            this.log("sender_sub_id option is required");
//            return;
//        }
//        if (this.config.getTargetSubID() == null || this.config.getTargetSubID().length() == 0) {
//            this.log("target_sub_id option is required");
//            return;
//        }
//        if (this.config.getPassword() == null || this.config.getPassword().length() == 0) {
//            this.log("password option is required");
//            return;
//        }
//        if (this.tbUrl == null || this.tbUrl.length() == 0) {
//            this.log("tburl option is required");
//            return;
//        }
////        if (params.isEmpty()) {
////            this.log("No request to send");
////            return;
////        }
//        this.log("Config: " + config.toString());
//        walker = new Walker(config, tbUrl, brokerID, config.getSecIdSrc());
//        walker.startSession();
//    }
//
//    private void log(final CharSequence text) {
//        System.out.println(text);
//
//    }
//
//    private class Walker extends SecurityParser {
//
//        private Iterator<SecurityRequestParamsList.SecurityRequestParams> iterator = null;
//        private final IdBasedMarketDataInitiatorFIXSession<? extends InitiatorFIXSessionConfig> session;
//        private final MarketDataInitiatorSessionListenerAdaptor listener = new MarketDataInitiatorSessionListenerAdaptor() {
//
//            @Override
//            public boolean beforeLogonSent(final OutputFIXMessage logon) {
////                log("1-" + DebugUtil.formatFixMessage(logon));
//                super.beforeLogonSent(logon);
////                log("1-" + DebugUtil.formatFixMessage(logon));
//                //OK>>>8=FIX.4.2;9=112;35=A;49=ENNK_MD;56=CME;50=NKowalski;57=70;52=20190325-10:11:22.759;34=1;98=0;108=30;141=Y;95=8;96=12345678;98=0;10=163
//                //XX>>>8=FIX.4.2;9=118;35=A;49=ENNK_MD;56=CME;50=NKowalski;57=70;52=20190325-10:01:52.594;34=1;98=0;108=30;141=Y;95=8;96=12345678;98=0;141=Y10=213
//                final String password = config.getPassword();
//                final OutputFIXMessage.Part body = logon.getBody();
////                body.addField(95, password.length());
//                body.addField(96, password);
////                body.addField(FIXField.EncryptMethod, 0);
////                body.addField(FIXField.MsgSeqNum, 1);
////                body.addField(FIXField.ResetSeqNumFlag, 'Y');
////                log("1-" + DebugUtil.formatFixMessage(logon));
//                return true;
//            }
//
//            @Override
//            public void onApplicationMessage(final int msgTypeCode, final InputFIXMessage msg) {
//                super.onApplicationMessage(msgTypeCode, msg);
//                try {
//                    if (msgTypeCode == 'd') {
//                        msg.walk(Walker.this);
//                        nextRequest();
//                    } else {
//                        log("Unsupported Message: " + msgTypeCode);
//                    }
//                } catch (Exception e) {
//                    log("Parsing error: " + e.getMessage());
//                    doQuit();
//                }
//            }
//
//            @Override
//            public void onLogon(final InputFIXMessage msg) {
//                super.onLogon(msg);
//                log("Logging in.");
//                Walker.this.iterator = params.iterator();
////                    nextRequest();
//            }
//
//            @Override
//            public void onLogout(final InputFIXMessage msg) {
//                super.onLogout(msg);
//                log("Logging out");
////                doQuit();
//            }
//
//            @Override
//            public void onStoppedByError(final Exception e) {
//                super.onStoppedByError(e);
//                e.printStackTrace();
//                log("Stopped by Error");
//                try {
//                    session.stop();
////                doQuit();
//                } catch (IOException | InterruptedException ex) {
//                    log("Error while logout: " + ex.getMessage());
//                }
//            }
//
//            private void nextRequest() {
//                try {
//                    if (iterator.hasNext()) {
//                        final SecurityRequest request = iterator.next().generateRequest(session);
//                        log("sending request: " + request);
//                        request.send();
//                    } else {
//                        doQuit();
//                    }
//                } catch (Exception ex) {
//                    log("Request error: " + ex.getMessage());
//                    ex.printStackTrace();
//                    doQuit();
//                }
//            }
//
//        };
//
//        Walker(final InitiatorFIXSessionConfig config, final String tbUrl, final String brokerID, final SecurityIDSource secIDSrc) {
//            super(tbUrl, brokerID, secIDSrc);
//            this.session = new IdBasedMarketDataInitiatorFIXSession<>(config);
//        }
//
//        @Override
//        public void onWalkFinish(final InputFIXMessage msg) throws FIXException {
//            super.onWalkFinish(msg);
//            try {
//                this.session.stop(3000L);
//                doQuit();
//            } catch (InterruptedException ex) {
//                doQuit();
//            } catch (Exception ex) {
//                throw new FIXException(ex);
//            }
//        }
//
//        public void startSession() throws IOException {
//            session.start(config.prepareChannel(), listener, true);
//        }
//
//        public void stopSession() throws IOException, InterruptedException {
//            session.stop(3000L);
//        }
//
//    }

}
