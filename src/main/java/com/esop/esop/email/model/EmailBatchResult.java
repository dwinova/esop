/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.email.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailBatchResult {
	private int totalEmails;
	
	private int successCount;
	
	private int failCount;
	
	
	public double getSuccessRate() {
		if (totalEmails == 0)
			return 0;
		return (double) successCount / totalEmails * 100;
	}
}
