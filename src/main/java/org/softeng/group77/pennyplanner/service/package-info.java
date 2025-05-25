/**
 * Service layer containing business logic and domain operations for the PennyPlanner application.
 * <p>
 * This package provides services that:
 * <ul>
 *   <li>Implement core business operations and workflows</li>
 *   <li>Coordinate between JavaFX UI and data access layers</li>
 *   <li>Manage transactions and ensure data integrity</li>
 *   <li>Execute business rules and validations</li>
 * </ul>
 * <p>
 * The service layer follows an interface-implementation pattern where each service
 * defines a contract through an interface and is implemented by one or more concrete classes.
 * <p>
 * Key services include:
 * <ul>
 *   <li>{@link org.softeng.group77.pennyplanner.service.AuthService} - User authentication and management</li>
 *   <li>{@link org.softeng.group77.pennyplanner.service.TransactionService} - Financial transaction operations</li>
 *   <li>{@link org.softeng.group77.pennyplanner.service.BudgetService} - Budget planning and tracking</li>
 *   <li>{@link org.softeng.group77.pennyplanner.service.ChartService} - Financial data visualization</li>
 *   <li>{@link org.softeng.group77.pennyplanner.service.TransactionAnalysisService} - AI-powered financial analysis</li>
 * </ul>
 *
 * @since 2.0.0
 * @author MA Ruize
 * @author CHAI Yihang
 * @author JIANG Mengnan
 * @author XI Yu
 * @version 2.0.0
 */
package org.softeng.group77.pennyplanner.service;