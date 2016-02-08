package com.learnopengles.sandbox.lesson8;

interface ErrorHandler {
	enum ErrorType {
		BUFFER_CREATION_ERROR
	}
	
	void handleError(ErrorType errorType, String cause);
}