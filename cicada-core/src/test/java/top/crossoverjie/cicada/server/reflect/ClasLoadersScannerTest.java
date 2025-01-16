package top.crossoverjie.cicada.server.reflect;

import org.junit.Test;
import top.crossoverjie.cicada.server.scanner.ClasLoadersScanner;

import java.util.Set;

public class ClasLoadersScannerTest {
    @Test
    public void getCicadaClasses() throws Exception {
        Set<Class<?>> classes = ClasLoadersScanner.getClasses("top.crossoverjie.cicada");
        System.out.println(classes.size());
    }

}