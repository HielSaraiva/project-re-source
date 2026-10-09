package edu.br.resource.resourcesystem.observability;

import java.util.Collections;
import java.util.IdentityHashMap;


public final class FailureDetails {
    private FailureDetails() {
    }

    public static String describe(Throwable failure) {
        var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
        var result = new StringBuilder();
        for (int depth = 0; failure != null && depth < 8 && seen.add(failure); depth++) {
            if (!result.isEmpty())
                result.append(" <- ");
            result.append(failure.getClass().getName());
            var frames = failure.getStackTrace();
            for (int index = 0; index < Math.min(frames.length, 3); index++)
                result.append(" at ").append(frames[index]);
            failure = failure.getCause();
        }
        return result.toString();
    }
}
