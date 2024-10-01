package com.jeju.nanaland.domain.report.entity;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class ReportStrategyFactory {
  private Map<ReportType, ReportStrategy> strategies;

  @Autowired
  public ReportStrategyFactory(Set<ReportStrategy> reportStrategies) {
    createStrategy(reportStrategies);
  }

  public ReportStrategy findStrategy(ReportType reportType) {
    return strategies.get(reportType);
  }
  private void createStrategy(Set<ReportStrategy> strategySet) {
    strategies = new EnumMap<>(ReportType.class);
    strategySet.forEach(
        strategy ->strategies.put(strategy.getReportType(), strategy));
  }
}
