package com.dace.dmgr;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 콘솔 로그 출력 기능을 제공하는 클래스.
 */
@UtilityClass
public final class ConsoleLogger {
    /** 플러그인 로거 인스턴스 */
    private static final Logger logger = DMGR.getPlugin().getLogger();

    /**
     * 일반 레벨의 로그를 출력한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // Hello, World!
     * ConsoleLogger.info("Hello, {0}!", "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public static void info(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        logger.info(() -> MessageFormat.format(message, arguments));
    }

    /**
     * 일반 레벨의 로그를 출력한다.
     *
     * @param message 메시지
     */
    public static void info(@NonNull String message) {
        logger.info(message);
    }

    /**
     * 경고 레벨의 로그를 출력한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // Hello, World!
     * ConsoleLogger.warn("Hello, {0}!", "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public static void warning(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        logger.warning(() -> MessageFormat.format(message, arguments));
    }

    /**
     * 경고 레벨의 로그를 출력한다.
     *
     * @param message 메시지
     */
    public static void warning(@NonNull String message) {
        logger.warning(message);
    }

    /**
     * 심각 레벨의 로그를 출력한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // Hello, World!
     * ConsoleLogger.severe("Hello, {0}!", "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param arguments 포맷에 사용할 인자 목록
     */
    public static void severe(@NonNull String message, @NonNull Object @NonNull ... arguments) {
        logger.severe(() -> MessageFormat.format(message, arguments));
    }

    /**
     * 심각 레벨의 로그를 출력한다.
     *
     * <p>지정한 arguments의 n번째 인덱스가 메시지에 포함된 '{n}'을 치환한다.</p>
     *
     * <p>Example:</p>
     *
     * <pre><code>
     * // "Hello, World!" 및 예외 정보 출력
     * ConsoleLogger.severe("Hello, {0}!", ex, "World");
     * </code></pre>
     *
     * @param message   메시지
     * @param exception 예외
     * @param arguments 포맷에 사용할 인자 목록
     */
    public static void severe(@NonNull String message, @NonNull Exception exception, @NonNull Object @NonNull ... arguments) {
        logger.log(Level.SEVERE, MessageFormat.format(message, arguments), exception);
    }

    /**
     * 심각 레벨의 로그를 출력한다.
     *
     * @param message 메시지
     */
    public static void severe(@NonNull String message) {
        logger.severe(message);
    }

    /**
     * 심각 레벨의 로그를 출력한다.
     *
     * @param message   메시지
     * @param exception 예외
     */
    public static void severe(@NonNull String message, @NonNull Exception exception) {
        logger.log(Level.SEVERE, message, exception);
    }
}
