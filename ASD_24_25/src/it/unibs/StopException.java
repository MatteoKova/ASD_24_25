package it.unibs;

class StopException extends RuntimeException {
    final RisultatoCammino partialResult;
    StopException(RisultatoCammino res) {
        this.partialResult = res;
    }
}