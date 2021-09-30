package cn.nihility.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExpressionUtils {


    private static final Map<String, Expression> EXPRESSION_CACHE = new ConcurrentHashMap<>(64);

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    /**
     * 获取Expression对象
     *
     * @param expressionString Spring EL 表达式字符串 例如 #{param.id}
     * @return Expression
     */
    @Nullable
    public static Expression getExpression(@Nullable String expressionString) {

        if (StringUtils.isBlank(expressionString)) {
            return null;
        }

        if (EXPRESSION_CACHE.containsKey(expressionString)) {
            return EXPRESSION_CACHE.get(expressionString);
        }

        Expression expression = SPEL_PARSER.parseExpression(expressionString);
        EXPRESSION_CACHE.put(expressionString, expression);
        return expression;
    }

    /**
     * 根据Spring EL表达式字符串从根对象中求值
     * 在自定义AOP中使用时,root为入参列表args[],Spring EL表达式只能用 [0].id,[1].name这种形式,不是很灵活,
     * 建议使用{@link ExpressionUtils#getExpressionValue(java.lang.Object[], java.lang.reflect.Method, java.lang.String...)}
     *
     * @param root             根对象
     * @param expressionString Spring EL表达式
     * @param clazz            值得类型
     * @param <T>              泛型
     * @return 值
     */
    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root,
                                           @Nullable String expressionString,
                                           @NonNull Class<? extends T> clazz) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }

        return expression.getValue(root, clazz);
    }

    @Nullable
    public static <T> T getExpressionValue(@Nullable Object root,
                                           @Nullable String expressionString) {
        if (root == null) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }

        //noinspection unchecked
        return (T) expression.getValue(root);
    }

    /**
     * 求值
     *
     * @param root              根对象
     * @param expressionStrings Spring EL表达式
     * @param <T>               泛型 这里的泛型要慎用,大多数情况下要使用Object接收避免出现转换异常
     * @return 结果集
     */
    public static <T> T[] getExpressionValue(@Nullable Object root, @Nullable String... expressionStrings) {
        if (root == null) {
            return null;
        }

        if (ArrayUtils.isEmpty(expressionStrings)) {
            return null;
        }

        //noinspection ConstantConditions
        Object[] values = new Object[expressionStrings.length];
        for (int i = 0; i < expressionStrings.length; i++) {

            values[i] = getExpressionValue(root, expressionStrings[i]);
        }
        //noinspection unchecked
        return (T[]) values;
    }

    /**
     * 表达式条件求值
     * 如果为值为null则返回false,
     * 如果为布尔类型直接返回,
     * 如果为数字类型则判断是否大于0
     *
     * @param root             根对象
     * @param expressionString Spring EL表达式
     * @return 值
     */
    public static Boolean getConditionValue(@Nullable Object root, @Nullable String expressionString) {
        Object value = getExpressionValue(root, expressionString);
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (boolean) value;
        }

        if (value instanceof Number) {
            return ((Number) value).longValue() > 0;
        }

        return false;
    }

    /**
     * 表达式条件求值
     *
     * @param root              根对象
     * @param expressionStrings Spring EL表达式数组
     * @return 值
     */
    @Nullable
    public static Boolean getConditionValue(@Nullable Object root, @Nullable String... expressionStrings) {

        if (root == null) {
            return false;
        }

        if (ArrayUtils.isEmpty(expressionStrings)) {
            return false;
        }

        //noinspection ConstantConditions
        for (String expressionString : expressionStrings) {
            if (!getConditionValue(root, expressionString)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 根据指定方法和方法入参获取EvaluationContext对象
     *
     * @param arguments 方法入参
     * @param method    方法
     * @return EvaluationContext
     */
    @NonNull
    @SuppressWarnings("ConstantConditions")
    public static EvaluationContext getEvaluationContext(@Nullable Object[] arguments, @NonNull Method method) {
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        if (ArrayUtils.isEmpty(arguments)) {
            return evaluationContext;
        }
        String[] parameterNames = new LocalVariableTableParameterNameDiscoverer().getParameterNames(method);
        for (int i = 0; i < arguments.length; i++) {
            evaluationContext.setVariable(parameterNames[i], arguments[i]);
        }
        return evaluationContext;
    }

    public static <T> T getExpressionValue(@Nullable EvaluationContext evaluationContext, @Nullable String expressionString) {
        if (evaluationContext == null || StringUtils.isBlank(expressionString)) {
            return null;
        }
        Expression expression = getExpression(expressionString);
        if (expression == null) {
            return null;
        }
        //noinspection unchecked
        return (T) expression.getValue(evaluationContext);
    }

    /**
     * 从指定方法上获取Spring EL表达式的值,支持以方法参数名作为Spring EL表达式的一部分
     * 例如方法声明为public void  method1(Person p1,String s1,Person p2);
     * 写法如下: #p1.id,#s1,#p2.name
     *
     * @param args               方法入参
     * @param method             方法
     * @param expressionsStrings Spring EL表达式
     * @param <T>                类型
     * @return 结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] getExpressionValue(@Nullable Object[] args, @NonNull Method method, @Nullable String... expressionsStrings) {
        if (expressionsStrings == null) {
            return null;
        }
        EvaluationContext evaluationContext = getEvaluationContext(args, method);
        Object[] result = new Object[expressionsStrings.length];
        for (int i = 0; i < expressionsStrings.length; i++) {
            result[i] = getExpressionValue(evaluationContext, expressionsStrings[i]);
        }
        return (T[]) result;
    }


    /**
     * 条件表达式
     *
     * @param args                        方法入参
     * @param method                      方法
     * @param conditionExpressionsStrings 表达式
     * @return conditionExpressionsStrings 表达式的解析结果全为真则为true, 其他情况为false
     */
    public static boolean getConditionValue(@Nullable Object[] args, @NonNull Method method, @Nullable String... conditionExpressionsStrings) {
        if (conditionExpressionsStrings == null) {
            return false;
        }

        EvaluationContext evaluationContext = getEvaluationContext(args, method);
        for (String conditionExpressionString : conditionExpressionsStrings) {
            if (!getConditionValue(evaluationContext, conditionExpressionString)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 从指定 EvaluationContext中依据 conditionExpressionString求出布尔值
     *
     * @param evaluationContext         EvaluationContext中依据
     * @param conditionExpressionString Spring EL表达式
     * @return true or false
     */
    public static boolean getConditionValue(@Nullable EvaluationContext evaluationContext, @Nullable String conditionExpressionString) {
        if (evaluationContext == null || StringUtils.isBlank(conditionExpressionString)) {
            return false;
        }

        Expression expression = getExpression(conditionExpressionString);
        if (expression == null) {
            return false;
        }

        Object conditionValue = expression.getValue(evaluationContext);
        if (conditionValue == null) {
            return false;
        }

        if (conditionValue instanceof Boolean) {
            return (boolean) conditionValue;
        }

        if (conditionValue instanceof Number) {
            return ((Number) conditionValue).longValue() > 0;
        }

        return false;
    }


    /**
     * 获取可以通过别名查找的EvaluationContext,类似于spring cache的用法 #a0.id,#p1.name
     *
     * @param arguments 方法入参
     * @param method    方法
     * @return MethodBasedEvaluationContext
     */
    @NonNull
    public static MethodBasedEvaluationContext getEvaluationContextAliasAble(@Nullable Object[] arguments, @NonNull Method method) {
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(null, method, arguments, new LocalVariableTableParameterNameDiscoverer());
        //通过查找空白的变量命间接加载方法的所有参数到当前EvaluationContext
        evaluationContext.lookupVariable("");
        return evaluationContext;
    }

    /**
     * 可以通过别名获取表达式的值,类似于spring cache的用法 可以给参数指定别名
     *
     * @param arguments        参数
     * @param method           方法
     * @param expressionString Spring EL表达式字符串
     * @param <T>              类型
     * @return 结果
     */
    @Nullable
    public static <T> T getExpressionValueAliasAble(@Nullable Object[] arguments, @NonNull Method method, String expressionString) {
        if (ArrayUtils.isEmpty(arguments) || StringUtils.isBlank(expressionString)) {
            return null;
        }

        Expression expression = getExpression(expressionString);

        if (expression == null) {
            return null;
        }

        MethodBasedEvaluationContext evaluationContext = getEvaluationContextAliasAble(arguments, method);

        //noinspection unchecked
        return (T) expression.getValue(evaluationContext);
    }

    @Nullable
    public static <T> T getExpressionValueAliasAble(@NonNull MethodBasedEvaluationContext evaluationContext, @Nullable String expressionString) {
        if (StringUtils.isBlank(expressionString)) {
            return null;
        }

        Expression expression = getExpression(expressionString);

        if (expression == null) {
            return null;
        }

        //noinspection unchecked
        return (T) expression.getValue(evaluationContext);
    }

    /**
     * 可以通过别名获取表达式的值,类似于spring cache的用法 可以给参数指定别名
     *
     * @param arguments         方法
     * @param method            参数
     * @param expressionsString Spring EL表达式字符串
     * @param <T>               类型
     * @return 结果集
     */
    @Nullable
    public static <T> T[] getExpressionValueAliasAble(@Nullable Object[] arguments, @NonNull Method method, String... expressionsString) {
        if (ArrayUtils.isEmpty(arguments) || ArrayUtils.isEmpty(expressionsString)) {
            return null;
        }

        Object[] result = new Object[expressionsString.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = getExpressionValueAliasAble(arguments, method, expressionsString[i]);
        }

        //noinspection unchecked
        return (T[]) result;
    }

    /**
     * 从指定 EvaluationContext中依据 conditionExpressionString求出布尔值
     *
     * @param evaluationContext         evaluationContext
     * @param conditionExpressionString conditionExpressionString
     * @return true or false
     */
    public static boolean getConditionValueAliasAble(MethodBasedEvaluationContext evaluationContext, String conditionExpressionString) {
        if (StringUtils.isBlank(conditionExpressionString)) {
            return false;
        }

        Expression expression = getExpression(conditionExpressionString);

        if (expression == null) {
            return false;
        }

        Object conditionValue = expression.getValue(evaluationContext);

        if (conditionValue == null) {
            return false;
        }

        if (conditionValue instanceof Boolean) {
            return (boolean) conditionValue;
        }

        if (conditionValue instanceof Number) {
            return ((Number) conditionValue).longValue() > 0;
        }

        return false;
    }


}
