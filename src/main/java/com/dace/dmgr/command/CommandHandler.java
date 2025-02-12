package com.dace.dmgr.command;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.user.User;
import com.dace.dmgr.util.StringFormUtil;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 명령어의 정보 및 응답과 인수 자동완성을 처리하는 클래스.
 */
public abstract class CommandHandler implements TabExecutor {
    /** 명령어 인스턴스 */
    private final PluginCommand command;
    /** 명령어의 매개변수 목록 */
    @Nullable
    private final ParameterList parameterList;
    /** 부 명령어 목록 */
    private final ArrayList<Subcommand> subcommands = new ArrayList<>();
    /** 키워드별 부 명령어 목록 (키워드 : 부 명령어) */
    private final HashMap<String, Subcommand> keywordsSubcommandMap = new HashMap<>();

    /**
     * 명령어 처리 인스턴스를 생성하고 등록한다.
     *
     * <p>명령어는 플러그인 설명 파일({@code plugins.yml})에 정의되어 있어야 한다.</p>
     *
     * @param name          플러그인 설명 파일에 정의된 명령어 이름
     * @param parameterList 명령어의 매개변수 목록
     * @throws NullPointerException  해당 이름의 명령어가 존재하지 않으면 발생
     * @throws IllegalStateException 해당 명령어의 CommandHandler가 이미 등록되었으면 발생
     */
    protected CommandHandler(@NonNull String name, @Nullable ParameterList parameterList) {
        this.command = DMGR.getPlugin().getCommand(name);
        Validate.validState(!(command.getExecutor() instanceof CommandHandler), "CommandHandler가 이미 등록됨");
        Validate.notNull(command, "명령어 %s가 존재하지 않음", name).setExecutor(this);

        this.parameterList = parameterList;
    }

    /**
     * 명령어 처리 인스턴스를 생성하고 등록한다.
     *
     * <p>명령어는 플러그인 설명 파일({@code plugins.yml})에 정의되어 있어야 한다.</p>
     *
     * @param name 플러그인 설명 파일에 정의된 명령어 이름
     * @throws NullPointerException  해당 이름의 명령어가 존재하지 않으면 발생
     * @throws IllegalStateException 해당 명령어의 CommandHandler가 이미 등록되었으면 발생
     */
    protected CommandHandler(@NonNull String name) {
        this(name, null);
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Subcommand subcommand = args.length > 0 ? keywordsSubcommandMap.get(args[0]) : null;
            if (subcommand == null)
                onCommandInput((Player) sender, args);
            else
                subcommand.onCommand((Player) sender, Arrays.copyOfRange(args, 1, args.length));
        }

        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length > 0) {
            int lastArg = args.length - 1;
            HashSet<String> completions = new HashSet<>();

            Subcommand subcommand = keywordsSubcommandMap.get(args[0]);
            if (subcommand == null) {
                if (parameterList != null)
                    completions.addAll(parameterList.getKeywords(lastArg));
                if (!subcommands.isEmpty())
                    completions.addAll(keywordsSubcommandMap.keySet());
            } else if (subcommand.parameterList != null && args.length > 1)
                completions.addAll(subcommand.parameterList.getKeywords(lastArg - 1));

            if (!completions.isEmpty())
                return completions.stream()
                        .filter(keyword -> keyword.toLowerCase().startsWith(args[lastArg].toLowerCase()))
                        .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    /**
     * 명령어의 사용법을 반환한다.
     *
     * @return 사용법
     */
    @NonNull
    protected final String getUsage() {
        return command.getUsage();
    }

    /**
     * 명령어의 설명을 반환한다.
     *
     * @return 설명
     */
    @NonNull
    protected final String getDescription() {
        return command.getDescription();
    }

    /**
     * 명령어의 도움말을 반환한다.
     *
     * @return 도움말
     */
    @NonNull
    protected final String getHelp() {
        return MessageFormat.format("§a§l{0} - §a{1}", getUsage(), getDescription());
    }

    /**
     * 명령어의 전체 도움말을 반환한다.
     *
     * <p>부 명령어가 지정된 경우 부 명령어의 도움말({@link Subcommand#getHelp()})을 모두 포함한다.</p>
     *
     * @return 전체 도움말
     */
    @NonNull
    protected final String getFullHelp() {
        if (subcommands.isEmpty())
            return getDescription();

        StringJoiner text = new StringJoiner("\n");

        text.add(StringFormUtil.BAR);
        if (parameterList != null)
            text.add(getHelp());
        text.add(subcommands.stream()
                .map(Subcommand::getHelp)
                .collect(Collectors.joining("\n")));
        text.add(StringFormUtil.BAR);

        return text.toString();
    }

    /**
     * 명령어 입력 시 실행할 작업.
     *
     * @param sender 입력자
     * @param args   인수 목록
     */
    protected abstract void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args);

    /**
     * 플레이어를 찾을 수 없을 때의 경고 메시지를 전송한다.
     *
     * @param sender 입력자
     */
    protected final void sendWarnPlayerNotFound(@NonNull Player sender) {
        User.fromPlayer(sender).sendMessageWarn("플레이어를 찾을 수 없습니다.");
    }

    /**
     * 명령어를 올바르지 않게 입력했을 때의 경고 메시지를 전송한다.
     *
     * @param sender 입력자
     */
    protected final void sendWarnWrongUsage(@NonNull Player sender) {
        User.fromPlayer(sender).sendMessageWarn("올바른 사용법: §n{0}", getUsage());
    }

    /**
     * 명령어의 매개변수 타입.
     */
    @AllArgsConstructor
    protected enum ParameterType {
        PLAYER_NAME(() -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList())),
        INTEGER(() -> Collections.singletonList("0")),
        STRING();

        /** 매개변수 키워드 목록 반환에 실행할 작업 */
        private final Supplier<List<String>> onGetKeywords;

        ParameterType() {
            this.onGetKeywords = Collections::emptyList;
        }
    }

    /**
     * 명령어의 매개변수 목록을 나타내는 클래스.
     */
    protected static final class ParameterList {
        /** 가변 인수 포함 여부 */
        private final boolean hasVarArgs;
        /** 매개변수 타입 목록 */
        private final ParameterType[] parameterTypes;

        /**
         * 명령어의 매개변수 목록 인스턴스를 생성한다.
         *
         * @param hasVarArgs     가변 인수 포함 여부. {@code true}로 지정하면 명령어 입력 시 마지막 인수를 가변적으로 지정할 수 있음
         * @param parameterTypes 매개변수 타입 목록
         */
        ParameterList(boolean hasVarArgs, @NonNull ParameterType @NonNull ... parameterTypes) {
            this.hasVarArgs = hasVarArgs;
            this.parameterTypes = parameterTypes;
        }

        /**
         * 명령어의 매개변수 목록 인스턴스를 생성한다.
         *
         * @param parameterTypes 매개변수 타입 목록
         */
        ParameterList(@NonNull ParameterType @NonNull ... parameterTypes) {
            this(false, parameterTypes);
        }

        /**
         * 지정한 인덱스에 해당하는 매개변수의 키워드 목록를 반환한다.
         *
         * @param index 인덱스
         * @return 키워드 목록
         */
        @NonNull
        private List<@NonNull String> getKeywords(int index) {
            int arg = Math.min(index, parameterTypes.length - 1);
            if (index < parameterTypes.length || hasVarArgs)
                return parameterTypes[arg].onGetKeywords.get();

            return Collections.emptyList();
        }
    }

    /**
     * 명령어의 부 명령어를 처리하는 클래스.
     */
    protected abstract class Subcommand {
        /** 사용법 */
        protected final String usage;
        /** 설명 */
        protected final String description;
        /** OP 필요 여부 */
        private final boolean isOpCommand;
        /** 매개변수 목록 */
        @Nullable
        private final ParameterList parameterList;

        /**
         * 부 명령어 처리 인스턴스를 생성하고 등록한다.
         *
         * @param usage         사용법
         * @param description   설명
         * @param isOpCommand   OP 필요 여부
         * @param parameterList 매개변수 목록
         * @param keywords      키워드 목록
         * @throws IllegalStateException 중복되는 키워드를 가진 Subcommand가 이미 등록되었으면 발생
         */
        protected Subcommand(@NonNull String usage, @NonNull String description, boolean isOpCommand, @Nullable ParameterList parameterList,
                             @NonNull String @NonNull ... keywords) {
            this.usage = getUsage().split(" ")[0] + " " + usage;
            this.description = description;
            this.isOpCommand = isOpCommand;
            this.parameterList = parameterList;

            subcommands.add(this);
            for (String keyword : keywords)
                Validate.validState(keywordsSubcommandMap.put(keyword, this) == null, "Subcommand가 이미 등록됨");
        }

        /**
         * 부 명령어 처리 인스턴스를 생성하고 등록한다.
         *
         * @param usage       사용법
         * @param description 설명
         * @param isOpCommand OP 필요 여부
         * @param keywords    키워드 목록
         * @throws IllegalStateException 중복되는 키워드를 가진 Subcommand가 이미 등록되었으면 발생
         */
        protected Subcommand(@NonNull String usage, @NonNull String description, boolean isOpCommand, @NonNull String @NonNull ... keywords) {
            this(usage, description, isOpCommand, null, keywords);
        }

        /**
         * 부 명령어 처리 인스턴스를 생성하고 등록한다.
         *
         * @param usage         사용법
         * @param description   설명
         * @param parameterList 매개변수 목록
         * @param keywords      키워드 목록
         * @throws IllegalStateException 중복되는 키워드를 가진 Subcommand가 이미 등록되었으면 발생
         */
        protected Subcommand(@NonNull String usage, @NonNull String description, @Nullable ParameterList parameterList,
                             @NonNull String @NonNull ... keywords) {
            this(usage, description, false, parameterList, keywords);
        }

        /**
         * 부 명령어 처리 인스턴스를 생성하고 등록한다.
         *
         * @param usage       사용법
         * @param description 설명
         * @param keywords    키워드 목록
         * @throws IllegalStateException 중복되는 키워드를 가진 Subcommand가 이미 등록되었으면 발생
         */
        protected Subcommand(@NonNull String usage, @NonNull String description, @NonNull String @NonNull ... keywords) {
            this(usage, description, false, null, keywords);
        }

        /**
         * 부 명령어 입력 시 실행할 작업.
         *
         * @see CommandHandler#onCommand(CommandSender, Command, String, String[])
         */
        private void onCommand(@NonNull Player sender, @NonNull String @NonNull [] args) {
            if (isOpCommand && !sender.isOp()) {
                User.fromPlayer(sender).sendMessageWarn("권한이 없습니다.");
                return;
            }

            onCommandInput(sender, args);
        }

        /**
         * 부 명령어의 도움말을 반환한다.
         *
         * @return 도움말
         */
        @NonNull
        protected final String getHelp() {
            return MessageFormat.format("§a§l{0} - §a{1}", usage, description);
        }

        /**
         * 명령어를 올바르지 않게 입력했을 때의 경고 메시지를 전송한다.
         *
         * @param sender 입력자
         */
        protected final void sendWarnWrongUsage(@NonNull Player sender) {
            User.fromPlayer(sender).sendMessageWarn("올바른 사용법: §n{0}", usage);
        }

        /**
         * 부 명령어 입력 시 실행할 작업.
         *
         * @param sender 입력자
         * @param args   인수 목록
         */
        protected abstract void onCommandInput(@NonNull Player sender, @NonNull String @NonNull [] args);
    }
}
