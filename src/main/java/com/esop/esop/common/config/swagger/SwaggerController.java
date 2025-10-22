/*
 * (C) 2025 Esop.
 *
 * NOTICE:  All source code, documentation and other information
 * contained herein is, and remains the property of Esop.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Esop.
 */
package com.esop.esop.common.config.swagger;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile({
	"local",
	"stage"
})
@Controller
public class SwaggerController {
	
	@GetMapping("/swagger")
	public String redirect() {
		return "redirect:/swagger-ui/index.html";
	}
}
