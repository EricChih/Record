/**
 * Copyright © 2020. Hualiteq International Corp.
 * All Rights Reserved.
 */
package com.ub.gir.web;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit test for simple Application.
 */
//@RunWith ( SpringJUnit4ClassRunner.class )
//@SpringBootTest ( classes = Application.class )
@SpringBootTest ( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT )
public class ApplicationTest
{

    /**
     * Rigorous Test :-)
     */
    @DisplayName ( "Test" )
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

}
