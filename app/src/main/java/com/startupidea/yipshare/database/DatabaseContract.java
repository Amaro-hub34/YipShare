package com.startupidea.yipshare.database;

public final class DatabaseContract {

    private DatabaseContract() {
    }

    public static final class Users {
        public static final String TABLE = "users";
        public static final String ID = "user_id";
        public static final String USERNAME = "username";
        public static final String EMAIL = "email";
        public static final String PHONE_NUMBER = "phone_number";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String NAME = "name";
        public static final String PATERNAL_SURNAME = "paternal_surname";
        public static final String MATERNAL_SURNAME = "maternal_surname";
        public static final String PROFILE_PICTURE = "profile_picture";
        public static final String COMPUTED_RATING = "computed_rating";
        public static final String STATE = "state";

        private Users() {
        }
    }

    public static final class Condominiums {
        public static final String TABLE = "condominiums";
        public static final String ID = "condominium_id";
        public static final String SHORT_CODE = "short_code";
        public static final String ADMIN_ID = "admin_id";
        public static final String NAME = "name";
        public static final String STREET = "street";
        public static final String NEIGHBORHOOD = "neighborhood";
        public static final String POSTAL_CODE = "postal_code";
        public static final String MUNICIPALITY = "municipality";
        public static final String CITY = "city";
        public static final String STATE = "state";
        public static final String DEED = "deed";
        public static final String MINUTES = "minutes";
        public static final String TAX_ID = "tax_id";
        public static final String IDENTIFICATION = "identification";

        private Condominiums() {
        }
    }

    public static final class Units {
        public static final String TABLE = "condominium_units";
        public static final String ID = "condominium_unit_id";
        public static final String CONDOMINIUM_ID = "condominium_id";
        public static final String NAME = "name";

        private Units() {
        }
    }

    public static final class Members {
        public static final String TABLE = "condominium_members";
        public static final String USER_ID = "user_id";
        public static final String CONDOMINIUM_ID = "condominium_id";
        public static final String UNIT_ID = "condominium_unit_id";
        public static final String CAN_REQUEST_LOANS = "can_request_loans";

        private Members() {
        }
    }

    public static final class PendingMembers {
        public static final String TABLE = "pending_condominium_members";
        public static final String USER_ID = "user_id";
        public static final String CONDOMINIUM_ID = "condominium_id";
        public static final String UNIT = "unit";
        public static final String SUGGESTED_UNIT_ID = "suggested_unit_id";

        private PendingMembers() {
        }
    }

    public static final class Articles {
        public static final String TABLE = "articles";
        public static final String ID = "article_id";
        public static final String CONDOMINIUM_ID = "condominium_id";
        public static final String LENDING_USER = "lending_user";
        public static final String NAME = "name";
        public static final String CATEGORY = "category";
        public static final String DESCRIPTION = "description";
        public static final String PHYSICAL_CONDITION = "physical_condition";
        public static final String IMAGE = "image";
        public static final String LOAN_DURATIONS = "loan_durations";
        public static final String AVAILABILITY = "availability";
        public static final String DELETED = "deleted";

        private Articles() {
        }
    }

    public static final class Loans {
        public static final String TABLE = "loans";
        public static final String ID = "loan_id";
        public static final String ARTICLE_ID = "article_id";
        public static final String LENDER_ID = "lender_id";
        public static final String USER_ID = "user_id";
        public static final String REQUEST_TEXT = "request_text";
        public static final String DAYS = "days";
        public static final String STATE = "state";
        public static final String CODE = "code";
        public static final String START_TIME = "start_time";
        public static final String RETURN_TIME = "return_time";

        private Loans() {
        }
    }

    public static final class Reviews {
        public static final String TABLE = "reviews";
        public static final String ID = "review_id";
        public static final String LOAN_ID = "loan_id";
        public static final String AUTHOR_ID = "author_id";
        public static final String USER_ID = "user_id";
        public static final String REVIEW_TEXT = "review_text";
        public static final String RATING = "rating";

        private Reviews() {
        }
    }

    public static final class Reports {
        public static final String TABLE = "reports";
        public static final String ID = "report_id";
        public static final String LOAN_ID = "loan_id";
        public static final String AUTHOR_ID = "author_id";
        public static final String USER_ID = "user_id";
        public static final String REPORT_TEXT = "report_text";
        public static final String STATE = "state";

        private Reports() {
        }
    }

    public static final class ReportEvidences {
        public static final String TABLE = "report_evidences";
        public static final String ID = "report_evidence_id";
        public static final String REPORT_ID = "report_id";
        public static final String IMAGE = "image";

        private ReportEvidences() {
        }
    }

    public static final class Notifications {
        public static final String TABLE = "notifications";
        public static final String ID = "notification_id";
        public static final String USER_ID = "user_id";
        public static final String HEADER = "header";
        public static final String BODY = "body";
        public static final String IS_READ = "is_read";

        private Notifications() {
        }
    }
}
