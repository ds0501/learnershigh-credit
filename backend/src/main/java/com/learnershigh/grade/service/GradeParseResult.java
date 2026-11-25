package com.learnershigh.grade.service;

import java.util.List;

public record GradeParseResult(List<ParsedGradeLine> lines, int failedCount) {
}
