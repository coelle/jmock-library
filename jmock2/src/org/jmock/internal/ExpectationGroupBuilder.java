package org.jmock.internal;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;
import org.hamcrest.core.IsEqual;
import org.hamcrest.core.IsInstanceOf;
import org.hamcrest.core.IsSame;
import org.jmock.api.Action;
import org.jmock.api.Expectation;
import org.jmock.api.ExpectationGroup;
import org.jmock.lib.Cardinality;
import org.jmock.lib.action.ActionSequence;
import org.jmock.lib.action.DoAllAction;
import org.jmock.lib.action.ReturnValueAction;
import org.jmock.lib.action.ThrowAction;
import org.jmock.syntax.ArgumentConstraintPhrases;
import org.jmock.syntax.CardinalityClause;
import org.jmock.syntax.MethodClause;
import org.jmock.syntax.ReceiverClause;

public class ExpectationGroupBuilder implements ExpectationBuilder, 
    CardinalityClause, ArgumentConstraintPhrases, ActionClause 
{
    private final ExpectationGroup group;
    private InvocationExpectationBuilder expectationBuilder;
    private List<ExpectationBuilder> elementBuilders = new ArrayList<ExpectationBuilder>();
    
    protected ExpectationGroupBuilder(ExpectationGroup expectationGroup) {
        this.group = expectationGroup;
    }
    
    private void initialiseExpectationCapture(Cardinality cardinality) {
        checkLastExpectationWasFullySpecified();
        
        expectationBuilder = new InvocationExpectationBuilder();
        expectationBuilder.setCardinality(cardinality);
        elementBuilders.add(expectationBuilder);
    }
    
    public void setDefaultAction(Action defaultAction) {
        for (ExpectationBuilder builder : elementBuilders) {
            builder.setDefaultAction(defaultAction);
        }
    }
    
    public Expectation toExpectation() {
        checkLastExpectationWasFullySpecified();
        
        for (ExpectationBuilder builder : elementBuilders) {
            group.add(builder.toExpectation());
        }
        
        return group;
    }
    
    private void checkLastExpectationWasFullySpecified() {
        if (expectationBuilder != null) {
            expectationBuilder.checkWasFullySpecified();
        }
    }
    
    /* 
     * Syntactic sugar
     */
    
    public ReceiverClause exactly(int count) {
        initialiseExpectationCapture(Cardinality.exactly(count));
        return expectationBuilder;
    }
    
    public <T> T one (T mockObject) {
        return exactly(1).of(mockObject);
    }
    
    public ReceiverClause atLeast(int count) {
        initialiseExpectationCapture(Cardinality.atLeast(count));
        return expectationBuilder;
    }
    
    public ReceiverClause between(int minCount, int maxCount) {
        initialiseExpectationCapture(Cardinality.between(minCount, maxCount));
        return expectationBuilder;
    }
    
    public ReceiverClause atMost(int count) {
        initialiseExpectationCapture(Cardinality.atMost(count));
        return expectationBuilder;
    }
    
    public MethodClause allowing(Matcher<Object> mockObjectMatcher) {
        return atLeast(0).of(mockObjectMatcher);
    }
    
    public <T> T allowing(T mockObject) {
        return atLeast(0).of(mockObject);
    }
    
    public <T> void ignoring(T mockObject) {
        atLeast(0).of(mockObject);
    }
    
    public <T> T never(T mockObject) {
        return exactly(0).of(mockObject);
    }
    
    private void addParameterMatcher(Matcher<?> matcher) {
        if (expectationBuilder == null) {
            throw new IllegalStateException(UnspecifiedExpectation.ERROR);
        }
        
        expectationBuilder.addParameterMatcher(matcher);
    }
    
    public <T> T with(Matcher<T> matcher) {
        addParameterMatcher(matcher);
        return null;
    }

    public boolean with(Matcher<Boolean> matcher) {
        addParameterMatcher(matcher);
        return Boolean.FALSE;
    }
    
    public byte with(Matcher<Byte> matcher) {
        addParameterMatcher(matcher);
        return 0;
    }

    public short with(Matcher<Short> matcher) {
        addParameterMatcher(matcher);
        return 0;
    }

    public int with(Matcher<Integer> matcher) {
        addParameterMatcher(matcher);
        return 0;
    }

    public long with(Matcher<Long> matcher) {
        addParameterMatcher(matcher);
        return 0;
    }

    public float with(Matcher<Float> matcher) {
        addParameterMatcher(matcher);
        return 0.0f;
    }

    public double with(Matcher<Double> matcher) {
        addParameterMatcher(matcher);
        return 0.0;
    }
    
    public void will(Action action) {
        if (expectationBuilder == null) {
            throw new IllegalStateException(UnspecifiedExpectation.ERROR);
        }
        
        expectationBuilder.setAction(action);
    }
    
    public void expects(ExpectationGroupBuilder subgroupBuilder) {
        elementBuilders.add(subgroupBuilder);
        expectationBuilder = null;
    }
    
    /* Common constraints
     */
    
    public <T> Matcher<T> equal(T value) {
        return new IsEqual<T>(value);
    }
    
    public <T> Matcher<T> same(T value) {
        return new IsSame<T>(value);
    }
    
    @SuppressWarnings("unused")
    public <T> Matcher<T> any(Class<T> type) {
        return new IsAnything<T>();
    }
    
    public <T> Matcher<T> anything() {
        return new IsAnything<T>();
    }
    
    public <T> Matcher<T> a(Class<T> type) {
        return new IsInstanceOf<T>(type);
    }
    
    public <T> Matcher<T> an(Class<T> type) {
        return new IsInstanceOf<T>(type);
    }
    
    /* Common actions
     */
    
    public Action returnValue(Object result) {
        return new ReturnValueAction(result);
    }
    
    public Action throwException(Throwable throwable) {
        return new ThrowAction(throwable);
    }
    
    public Action doAll(Action...actions) {
        return new DoAllAction(actions);
    }
    
    public Action onConsecutiveCalls(Action...actions) {
        return new ActionSequence(actions);
    }
}
