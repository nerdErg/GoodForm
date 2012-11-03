package com.nerderg.goodForm

import grails.test.*
import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONArray

class GoodFormServiceTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testFindField() {
        def goodFormService = new GoodFormService()
        assert goodFormService.findField([Q1: [question_one: [yes: 'on']]], "Q1.question_one.yes") == 'on'
        assert goodFormService.findField([Q1: [question_one: [yes: 'on']]], "Q1.question_one.yes.on") == null
        assert goodFormService.findField([Q1: ''], "Q1.question_one") == null
        assert goodFormService.findField([:], "Q1.question_one") == null

        assert goodFormService.findField([Q1: [question_one: [firstName: ['Peter', 'James', 'David']]]], "Q1.question_one.firstName", 0) == 'Peter'
        assert goodFormService.findField([Q1: [question_one: [firstName: ['Peter', 'James', 'David']]]], "Q1.question_one.firstName", 2) == 'David'
        assert goodFormService.findField([Q1: [question_one: [firstName: ['Peter', 'James', 'David']]]], "Q1.question_one.firstName") == ['Peter', 'James', 'David']
        assert goodFormService.findField([Q1: [question_one: [firstName: ['Peter', 'James', 'David']]]], "Q1.question_one") == [firstName: ['Peter', 'James', 'David']]

        String[] names = ['John', 'Malcom', 'Wal', 'Adam']
        assert goodFormService.findField([Q1: [question_one: [firstName: names]]], "Q1.question_one.firstName", 1) == 'Malcom'

        Map formData = JSON.parse(familyFormDataJson) as Map
        assert goodFormService.findField(formData, "F2.Does_this_matter_involve_children.Please_list_the_children.children.Given_Names") instanceof JSONArray
        assert goodFormService.findField(formData, "F2.Does_this_matter_involve_children.Please_list_the_children.children.Given_Names", 0) == 'Linda'
        assert goodFormService.findField(formData, "G5.Gender", 0) == 'Female'

    }

    void testSetField() {
        def goodFormService = new GoodFormService()
        Map m = [Q1: [question_one: [yes: 'on']]]
        assert goodFormService.findField(m, "Q1.question_one.yes") == 'on'
        goodFormService.setField(m, "Q1.question_one.yes", "off")
        assert m.Q1.question_one.yes == 'off'
        assert m == [Q1: [question_one: [yes: 'off']]]
        goodFormService.setField(m, "Q1.question_one.wobble", [horiz: 12, vert: 1])
        assert m == [Q1: [question_one: [yes: 'off', wobble: [horiz: 12, vert: 1]]]]
    }

    void testRemoveField() {
        def goodFormService = new GoodFormService()
        Map m = [Q1: [question_one: [yes: 'off', wobble: [horiz: 12, vert: 1]]]]
        goodFormService.removeField(m, "Q1.question_one.wobble")
        assert m == [Q1: [question_one: [yes: 'off']]]
    }

    void testPrintFormDataAnswer() {
        def goodFormService = new GoodFormService()
        Map formData = JSON.parse(familyFormDataJson) as Map
        formData.each { key, data ->
            if (data instanceof Map) {
                String result = goodFormService.printFormDataAnswer(data as Map, '')
                println result
            }
        }
    }

    String familyFormDataJson = """{
  "M8": {"order": "4"},
  "M13": {
    "order": "0",
    "What_housing_payments_do_you_make_each_week": {"housing": {
      "Mortgage": "",
      "Board": "",
      "None": "",
      "Rent": 200
    }}
  },
  "M7": {
    "order": "3",
    "Do_you_have_a_Health_Care_Card_or_Pensioner_Concession_Card": {
      "Card_Number": "",
      "Expiry_date": "",
      "Attach_a_copy_of_the_card": "none"
    }
  },
  "M12": {"order": "3"},
  "C5F": {
    "Which_court_or_tribunal_do_you_have_to_go_to": "Family Court",
    "order": "0",
    "Which_court_or_tribunal_do_you_have_to_go_to_Other": {"Give_details": ""}
  },
  "M6": {"order": "2"},
  "M11": {
    "order": "2",
    "What_is_your_total_weekly_work_income": {"workIncome": {
      "Attach_a_copy_of_a_recent_payslip_or_other_proof_of_income": "M11.Attach_a_copy_of_a_recent_payslip_or_other_proof_of_income-avatar2.0.png",
      "Amount": 500
    }}
  },
  "M10": {"order": "1"},
  "M5": {
    "order": "1",
    "Do_you_get_a_pension_or_benefit_from_Centrelink_or_the_Department_of_Veterans_Affairs": {
      "Centrelink_Reference_Number_CRN_or_DVA_reference_number": "",
      "Weekly_income_before_tax": "",
      "Which_payments": {"Other": {"give_details": ""}}
    }
  },
  "M9": {
    "order": "0",
    "Do_you_get_any_other_income_or_benefits": {"otherIncome": {
      "Do_you_get_any_other_income": {
        "Amount": "",
        "Type": ""
      },
      "Do_you_get_rental_assistance": {"Amount": ""},
      "Do_you_get_workers_compensation_payments": {"Amount": ""},
      "Do_you_get_board": {"Amount": ""},
      "Do_you_get_an_allowance": {"Amount": ""},
      "Do_you_get_overtime_payments": {"Amount": ""},
      "Do_you_get_any_commission": {"Amount": ""},
      "Do_you_get_child_or_spouse_support": {"Amount": ""},
      "Do_you_get_income_from_a_trust": {"Amount": ""},
      "Do_you_get_paid_interest_payments": {"Amount": ""},
      "Do_you_get_superanuation_payments": {"Amount": ""}
    }}
  },
  "applicationId": "9",
  "S1": {
    "order": "3",
    "Have_you_represented_this_client_before_in_a_Legal_Aid_assisted_case": {"Please_enter_a_recent_previous_Legal_Aid_ACT_file_number": ""},
    "pass": false
  },
  "M4": {
    "order": "0",
    "Are_you_currently_employed_own_a_small_business_or_a_farmer": {
      "yes": "on",
      "What_type_of_work_do_you_do": "Shop assistant"
    }
  },
  "M3": {
    "order": "2",
    "Is_there_anyone_who_could_be_resonably_expected_to_financially_assit_you": {"Please_list_these_people": {"faps": {
      "Relationship_to_you": "",
      "Full_name": ""
    }}}
  },
  "M2": {
    "order": "1",
    "Do_you_have_any_dependant_children": {
      "yes": "on",
      "How_many": 2
    }
  },
  "M1": {
    "order": "0",
    "Do_you_have_a_spouse_or_partner_living_with_you": "on"
  },
  "G12": {
    "order": "0",
    "Breifly_explain_your_legal_problem": "I want the kids all to myself"
  },
  "firmNumber": "30043",
  "action": "applyNext",
  "M18": {"order": "1"},
  "M19": {
    "order": "2",
    "Do_you_own_anything_else_of_value": {"Please_list": {"otherAssets": {
      "Description": "",
      "Appoximate_value": ""
    }}}
  },
  "M16": {"order": "3"},
  "G10": {
    "order": "0",
    "Contact_details": {"contact": {
      "Work_Phone": 624766552,
      "Postcode": 2614,
      "Can_we_contact_you_by_email": {
        "yes": "on",
        "address": "llove@gmail.com"
      },
      "Home_address": "45 Love St\\r\\nPage ACT",
      "Can_we_contact_you_by_SMS": "on",
      "Can_we_contact_you_at_this_address": {
        "Postcode": "",
        "yes": "on",
        "Please_provide_a_contact_address": ""
      },
      "Home_Phone": 62547766,
      "Mobile_Phone": 418482545
    }}
  },
  "M17": {
    "order": "0",
    "Do_you": {"assets": {
      "Own_or_are_paying_off_any_other_real_estate": {
        "Postcode": "",
        "Address_of_the_real_estate": "",
        "What_share_of_the_real_estate_is_yours": "",
        "What_is_the_market_value_of_the_real_estate": "",
        "How_much_is_owed_on_the_real_estate": ""
      },
      "Own_or_are_paying_off_any_motor_vehicles": {
        "yes": "on",
        "How_many": 1,
        "How_much_is_owed_on_the_vehicles": 4000,
        "What_is_the_total_market_value_of_the_vehicles": 12000,
        "What_share_of_the_vehicles_is_yours": 100
      },
      "Have_any_bank_credit_union_or_building_society_accounts": {
        "yes": "on",
        "Please_list_accounts": {"bankAccounts": {
          "Name_of_bank_credit_union_or_building_society": "Commonwealth Bank",
          "Account_balance": 23.5,
          "What_share_of_the_account_is_yours": 100,
          "Account_number": "99882266554422"
        }}
      },
      "Have_any_cash": {
        "What_share_of_the_cash_is_yours": "",
        "Total_amount": ""
      },
      "Own_or_are_paying_off_a_home": {
        "How_long_have_you_lived_there": "",
        "What_is_the_market_value_of_the_home": "",
        "What_year_did_you_buy_the_home": "",
        "What_share_of_the_home_is_yours": "",
        "How_much_is_owed_on_the_home": ""
      }
    }}
  },
  "M14": {"order": "1"},
  "M15": {
    "order": "2",
    "Do_you_pay_any_of_the_following": {"otherPayments": {
      "Child_support": {
        "Amount": "",
        "How_many_children": ""
      },
      "Spouse_maintenance": {"Amount": ""},
      "Child_care_fees": {"Amount": ""}
    }}
  },
  "expenseCalc": {
    "fapChildCare": 0,
    "dependantsDeduction": 484.89,
    "dependants": 3,
    "childSupport": 0,
    "housing": 200,
    "childCare": 0,
    "fapChildSupport": 0,
    "fapHousing": 0
  },
  "F8": {
    "Does_the_person_you_are_in_dispute_with_have_a_lawyer": "I don’t know",
    "order": "4",
    "Does_the_person_you_are_in_dispute_with_have_a_lawyer_Yes": {
      "Phone": "",
      "Email_address": "",
      "Lawyers_name": "",
      "Law_firm": ""
    }
  },
  "primaryMatterType": "455675: (FCRE) Child Representation Care",
  "F9": {
    "order": "5",
    "Have_you_been_to_counselling_mediation_or_dispute_resolution": {
      "yes": "on",
      "Attach_a_copy_of_the_family_dispute_resolution_certificate": "F9.Attach_a_copy_of_the_family_dispute_resolution_certificate-DSCF0012.JPG"
    }
  },
  "F6": {"order": "2"},
  "F7": {
    "order": "3",
    "Details_of_the_other_person_involved_in_the_dispute": {"otherParty": {
      "Given_Names": "Arnold Henry",
      "Work_Phone": "",
      "Relationship_to_you_Other": {"Relationship": ""},
      "Email_address": "alove@gmail.com",
      "Date_of_birth": "4/5/1962",
      "Home_Phone": 625433445,
      "Relationship_to_you": "Married",
      "Relationship_to_you_de_facto": {
        "Date_separated": "",
        "Date_relationship_started": ""
      },
      "Last_or_Family_Name": "Love",
      "Mobile_Phone": 418556677,
      "Relationship_to_you_Married": {
        "Date_married": "19/8/1990",
        "Date_separated": "19/8/2009",
        "Date_divorced": "1/12/2010"
      }
    }}
  },
  "fap": [],
  "F1": {
    "order": "0",
    "Are_you_applying_for_assistance_to_respond_to_court_application": "on"
  },
  "F5": {"order": "1"},
  "F4": {
    "order": "0",
    "Are_there_existing_court_orders_in_relation_to_this_dispute": {"Attach_a_copy_of_the_orders": "none"}
  },
  "F3": {
    "order": "2",
    "What_family_law_matter_do_you_want_legal_aid_for": {"matterType": {
      "Child_raising_arrangements": "on",
      "Other": {
        "yes": "on",
        "Give_details": "wiggly waffles"
      },
      "Property_settlement": {"Other": {"Give_details": ""}},
      "Who_a_child_lives_with": "on"
    }}
  },
  "F2": {
    "order": "1",
    "Does_this_matter_involve_children": {
      "yes": "on",
      "Please_list_the_children": {"children": {
        "Given_Names": [
          "Linda",
          "Larry"
        ],
        "Date_of_birth": [
          "20/7/2003",
          "11/9/2005"
        ],
        "Relationship_to_you": [
          "daughter",
          "Son"
        ],
        "Last_or_Family_Name": [
          "Love",
          "Love"
        ],
        "Does_this_child_live_with_you": {"yes": [
          "on",
          "on"
        ]}
      }},
      "Have_any_of_the_children_already_been_removed": {
        "yes": "on",
        "Date_removed": "01/04/2012"
      },
      "Is_someone_alleging_a_risk_to_the_safety_or_welfare_of_children": "on"
    }
  },
  "AF": {
    "Which_court_or_tribunal_made_the_original_decision": "Family Court",
    "order": "0",
    "Which_court_or_tribunal_made_the_original_decision_Other": {"Give_details": ""}
  },
  "require": [],
  "controller": "grant",
  "M31": {
    "message": "You probably pass the means test on income -184.89",
    "order": "6",
    "Has_any_person_or_group_offered_to_pay_or_are_they_able_to_pay_any_of_your_legal_fees_for_this_case": {"Please_give_details": ""}
  },
  "M30": {"order": "5"},
  "applyNext": "Continue",
  "C4": {
    "Where_is_the_court_or_tribunal": {"courtOrTribunal": {
      "State": "ACT",
      "Town_or_City": "Canberra"
    }},
    "order": "2"
  },
  "C3": {
    "order": "1",
    "What_date_was_the_original_decision_made": "07/02/2012"
  },
  "next": ["End"],
  "G7": {
    "order": "0",
    "Do_you_have_any_special_circumstances": {"What_type": {"Other": {"Give_details": ""}}}
  },
  "loginType": "PP",
  "G9": {
    "Are_you": {"restriction": {"In_prison_or_detained": {"Other": {"Give_details": ""}}}},
    "order": "2"
  },
  "G4": {
    "order": "1",
    "Date_of_birth": "19/2/1967"
  },
  "G3": {
    "order": "0",
    "Have_you_applied_for_legal_aid_before": {"In_which_year": ""}
  },
  "G6": {
    "Country_of_birth": "AUSTRALIA",
    "order": "1"
  },
  "G5": {
    "order": "2",
    "Gender": "Female"
  },
  "G2": {
    "order": "0",
    "What_is_your_name": {"names": {
      "Have_you_been_or_are_you_known_by_any_other_names": {"List_your_other_names": {"aliases": {
        "Type_of_name": "",
        "Other_name": ""
      }}},
      "Given_Names": "Lucy",
      "Last_or_Family_Name": "Love",
      "Title": "Ms"
    }}
  },
  "C1": {
    "order": "1",
    "What_type_of_case_is_this": "Family"
  },
  "C2": {
    "Do_you_want_to_appeal_a_decision_of_a_court_or_tribunal": "on",
    "order": "2"
  },
  "L1": {
    "Do_you_have_a_lawyer_representing_you": {
      "Phone": "",
      "Email_address": "",
      "Lawyers_name": "",
      "Law_firm": ""
    },
    "order": "1"
  },
  "L2": {
    "order": "0",
    "Do_you_have_a_preference_for_a_particular_lawyer_Yes_another_lawyer": {
      "Postcode": "",
      "Phone": "",
      "Email_address": "",
      "Lawyers_name": "",
      "Law_firm": "",
      "Address": ""
    },
    "Do_you_have_a_preference_for_a_particular_lawyer": "Yes, a Legal Aid ACT lawyer"
  },
  "M20": {"order": "3"},
  "M22": {"order": "1"},
  "M21": {
    "Does_anyone_owe_you_money": {"How_much_is_owed": ""},
    "order": "0"
  },
  "M24": {"order": "3"},
  "M23": {
    "During_the_past_12_months_have_you_sold_or_given_away_property_worth_500_or_more": {"Please_list": {"soldProperty": {
      "Description": "",
      "Value_or_amount": ""
    }}},
    "order": "2"
  },
  "AU1": {
    "order": "0",
    "Are_you_completing_this_application_on_behalf_of_someone_else": {"What_authority_do_you_have_to_complete_the_application_for_that_person_Other": {"Give_details": ""}}
  },
  "AU2": {
    "order": "1",
    "Do_you_authorise_anyone_else_to_be_given_information_concerning_this_application_if_they_request_it": {
      "Postcode": "",
      "Address": "",
      "Their_Given_Names": "",
      "Last_or_Family_Name": ""
    }
  },
  "M25": {
    "order": "0",
    "During_the_past_12_months_have_you_given_away_500_or_more": {"Please_list": {"givenMoney": {
      "Description": "",
      "Value_or_amount": ""
    }}}
  },
  "M26": {"order": "1"},
  "M27": {
    "order": "2",
    "During_the_past_12_months_have_you_recieved_any_other_money_or_property_worth_500_or_more": {"Please_list": {"receivedMoneyProperty": {
      "Description": "",
      "Value_or_amount": ""
    }}}
  },
  "M28": {"order": "3"},
  "M29": {
    "order": "4",
    "During_the_next_12_months_are_you_likely_to_receive_any_lump_sum_amount_of_money": {"Please_list": {"receiveLumpSum": {
      "Description": "",
      "Value_or_amount": ""
    }}}
  },
  "incomeCalc": {
    "work": 500,
    "other": 0,
    "fapBenefits": 0,
    "fapOther": 0,
    "fapWork": 0,
    "assessableIncome": -184.89,
    "benefits": 0
  }
}"""
}
