package org.curator.core;

import org.curator.common.exceptions.CuratorException;

import java.util.Scanner;

public final class HtmlUtil {

    private HtmlUtil() {
    }

    public static String render(final String html) throws CuratorException {

        if (html == null) {
            throw new IllegalArgumentException("No data available");
        }

        try {
            final ProcessBuilder builder = new ProcessBuilder("w3m", "-dump", "-T", "text/html");

            Scanner scanner = null;
            Process process = null;
            final StringBuilder sbuffer = new StringBuilder(html.length() / 3);
            try {

                process = builder.start();
                process.getOutputStream().write(html.getBytes());
                process.getOutputStream().close();

                scanner = new Scanner(process.getInputStream());
                while (scanner.hasNextLine()) {
                    final String line = scanner.nextLine();
                    sbuffer.append(line);
                    sbuffer.append(' ');
                }
            } finally {
                if (scanner != null) {
                    scanner.close();
                }
                if (process != null) {
                    try {
                        process.getErrorStream().close();
                    } catch (Exception ignored) {
                    }
                    try {
                        process.getInputStream().close();
                    } catch (Exception ignored) {
                    }
                    try {
                        process.getOutputStream().close();
                    } catch (Exception ignored) {
                    }
                    process.destroy();
                }
            }

            return sbuffer.toString();
        } catch (Exception e) {
            throw new CuratorException("Cannot render html.", e);
        }

    }
}
