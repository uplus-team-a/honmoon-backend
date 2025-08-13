-- (1) public.users 테이블, supabase auth 테이블 연동 (이미 되어있으므로 생략)
CREATE TABLE IF NOT EXISTS public.users
(
    id                uuid PRIMARY KEY REFERENCES auth.users (id) ON DELETE CASCADE,
    email             varchar(255),
    nickname          varchar(255),
    total_points      integer                           default 0 not null,
    total_activities  integer                           default 0 not null,
    profile_image_url varchar(255),
    password_hash     varchar(255),
    created_by        VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    modified_by       VARCHAR(255)             NOT NULL DEFAULT 'anonymous',
    modified_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    is_active         boolean                           default true
);

-- (2) 트리거 함수
CREATE OR REPLACE FUNCTION public.handle_new_user()
    RETURNS trigger AS
$$
BEGIN
    INSERT INTO public.users (id, email, nickname, created_at)
    VALUES (NEW.id,
            NEW.email,
            NEW.raw_user_meta_data ->> 'nickname',
            timezone('utc', now()));
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- (3) 트리거
CREATE TRIGGER on_auth_user_created
    AFTER INSERT
    ON auth.users
    FOR EACH ROW
EXECUTE PROCEDURE public.handle_new_user();
