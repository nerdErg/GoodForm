package com.nerderg.goodForm

import grails.test.mixin.TestFor
import org.junit.Before

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(AddressWranglingService)
class AddressWranglingServiceTests {

    def addressWranglingService

    @Before void setUp() {
        addressWranglingService = new AddressWranglingService(grailsApplication: [config: [goodform: [data: [directory: '.']]]])
    }

    void testGetSuburbsByPostcode() {
        long start = System.currentTimeMillis()
        Set suburbs = addressWranglingService.getSuburbsByPostcode(2614)
        println "Read and map postcodes took ${System.currentTimeMillis() - start}ms"
        println suburbs
        ['JAMISON CENTRE', 'PAGE', 'COOK', 'ARANDA', 'MACQUARIE', 'WEETANGERA', 'HAWKER', 'SCULLIN'].each {
            assert suburbs.contains(it)
        }
        start = System.currentTimeMillis()
        suburbs = addressWranglingService.getSuburbsByPostcode(2913)
        println "get map postcodes took ${System.currentTimeMillis() - start}ms"
        println suburbs
        ['KINLYSIDE', 'FRANKLIN', 'PALMERSTON', 'NICHOLLS', 'NGUNNAWAL', 'CASEY', 'TAYLOR', 'GINNINDERRA VILLAGE'].each {
            assert suburbs.contains(it)
        }
    }

    void testIsPostcodeInState() {
        assert addressWranglingService.isPostcodeInState('ACT', 2614)
        assert !addressWranglingService.isPostcodeInState('NT', 2614)
        assert addressWranglingService.isPostcodeInState('NT', 810)
    }

    void testFindSuburb() {
        String suburb = addressWranglingService.findSuburb("32 Henry St. Cook", "2614")
        assert suburb == "COOK"
        suburb = addressWranglingService.findSuburb("32 Ainsley St. Cook", "2614")
        assert suburb == "COOK"
        suburb = addressWranglingService.findSuburb("32 Weetangera St. Cook", "2614")
        assert suburb == "COOK"
        suburb = addressWranglingService.findSuburb("32 Weetangera St. Darmody", "2614")
        assert suburb == "WEETANGERA"
        suburb = addressWranglingService.findSuburb("12/45 findit St. Jamison Centre", "2614")
        assert suburb == "JAMISON CENTRE"
        suburb = addressWranglingService.findSuburb("POBox 1245 Jamison Centre St., Weetangera", "2614")
        assert suburb == "WEETANGERA"
        suburb = addressWranglingService.findSuburb("POBox 1245 Jamison St., fredoville", "2614")
        assert suburb == null
        suburb = addressWranglingService.findSuburb("POBox 1245 Jamison Centre St., Weetangera", "2509")
        assert suburb == null
        suburb = addressWranglingService.findSuburb("POBox 1245 Jamison Centre St., Weetangera", "")
        assert suburb == null
        suburb = addressWranglingService.findSuburb("POBox 1245 Jamison Centre St., Weetangera", null)
        assert suburb == null
        suburb = addressWranglingService.findSuburb("", "2614")
        assert suburb == null
        suburb = addressWranglingService.findSuburb(null, "2614")
        assert suburb == null
    }

    void testGetState() {
        assert 'ACT' == addressWranglingService.getState('2614')
        assert 'NSW' == addressWranglingService.getState('2000')
        assert 'NT' == addressWranglingService.getState('810')
        assert null == addressWranglingService.getState('2509')
        assert null == addressWranglingService.getState('')
        assert null == addressWranglingService.getState(null)
    }

    void testValidPostcode() {
        assert addressWranglingService.isValidPostcode('2614')
        assert !addressWranglingService.isValidPostcode('2509')
        assert !addressWranglingService.isValidPostcode('')
        assert !addressWranglingService.isValidPostcode(null)
    }
}
