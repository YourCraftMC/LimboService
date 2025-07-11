package cn.ycraft.limbo.command.defaults;

import com.loohp.limbo.Console;
import com.sun.management.HotSpotDiagnosticMXBean;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

import javax.management.MBeanServer;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;


@Command(name = "dump")
@Permission("limbo.command.dump")
public class DumpCommand {

    @Execute
    public void execute(@Sender Console console, @Arg("path") String path) throws Exception {
        File targetDir = new File(path);

        if (!targetDir.exists() && !targetDir.mkdirs()) {
            throw new IOException("Failed to create directory: " + targetDir);
        }
        if (!targetDir.isDirectory()) {
            throw new IllegalArgumentException("Target path is not a directory: " + targetDir);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String fileName = "heapdump_" + timestamp + ".hprof";
        File dumpFile = new File(targetDir, fileName);

        console.sendMessage("Dump heap to " + dumpFile.getAbsolutePath());

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(
            server,
            "com.sun.management:type=HotSpotDiagnostic",
            HotSpotDiagnosticMXBean.class
        );

        mxBean.dumpHeap(dumpFile.getAbsolutePath(), false);
    }

}
